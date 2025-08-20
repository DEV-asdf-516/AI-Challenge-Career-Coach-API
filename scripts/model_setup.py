#!/usr/bin/env python3
import os, sys, json, glob, time, shutil, subprocess, threading, hashlib
import urllib.request, urllib.error
import requests  # <-- add requests for streaming upload

HOST = os.environ.get("OLLAMA_HOST_URL", "http://ollama:11434").rstrip("/")
NAME = os.environ.get("OLLAMA_MODEL", "benedict/linkbricks-llama3.1-korean:8b")

def log(msg): print(msg, flush=True)

# ---------- HTTP helpers ----------
def _http_post(url, payload=None, headers=None, stream=False):
    data = None if payload is None else json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(url, data=data, headers=headers or {"Content-Type":"application/json"})
    if stream:
        return urllib.request.urlopen(req)  # caller consumes lines
    else:
        with urllib.request.urlopen(req) as r:
            return r.read()

def stream_post(path, payload):
    try:
        with _http_post(f"{HOST}{path}", payload, stream=True) as r:
            for raw in r:
                line = raw.decode("utf-8", "ignore").strip()
                if not line: continue
                try:
                    obj = json.loads(line)
                except Exception:
                    log(f"[ollama] {line}")
                    continue
                if "status" in obj:
                    st = obj.get("status")
                    completed, total = obj.get("completed"), obj.get("total")
                    pct = f" {int(completed/total*100)}%" if (completed and total) else ""
                    log(f"🟡 {st}{pct}")
                if obj.get("error"): raise RuntimeError(obj["error"])
                if obj.get("success") is True:
                    log("✅ 완료"); return obj
        log("ℹ️ 스트림 종료"); return None
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8", "ignore")
        raise RuntimeError(f"Ollama HTTP {e.code} @ {path}: {body}")
    except urllib.error.URLError as e:
        raise RuntimeError(f"Ollama 연결 실패 @ {path}: {e}")

def post_ok(path, payload):
    _http_post(f"{HOST}{path}", payload)
    return True

def exists(model_name):
    try:
        post_ok("/api/show", {"model": model_name})
        return True
    except Exception:
        return False

# ---------- utils ----------
def human(n):
    for u in ["B","KB","MB","GB","TB"]:
        if n < 1024: return f"{n:.1f}{u}"
        n /= 1024
    return f"{n:.1f}PB"

def sha256_of_file(fp, chunk=8*1024*1024):
    h = hashlib.sha256()
    with open(fp, "rb") as f:
        while True:
            b = f.read(chunk)
            if not b: break
            h.update(b)
    return "sha256:" + h.hexdigest()

def blob_exists(digest):
    # HEAD /api/blobs/:digest
    url = f"{HOST}/api/blobs/{digest}"
    try:
        r = requests.head(url, timeout=30)
        return r.status_code == 200
    except Exception:
        return False

def upload_blob_with_digest(fp, digest):
    # POST /api/blobs/:digest (raw bytes)
    url = f"{HOST}/api/blobs/{digest}"
    with open(fp, "rb") as f:
        r = requests.post(url, data=f, headers={"Content-Type":"application/octet-stream"}, timeout=None)
    r.raise_for_status()

# ---------- main ----------
def main():
    log(f"🔧 OLLAMA_HOST_URL = {HOST}")
    log(f"🔧 OLLAMA_MODEL    = {NAME}")

    os.makedirs("/models/.tmp", exist_ok=True)
    os.environ.setdefault("HF_HUB_TEMP_DIR", "/models/.tmp")
    os.environ.setdefault("HF_HUB_ENABLE_PROGRESS_BARS", "1")
    os.environ.setdefault("HF_HUB_ENABLE_HF_TRANSFER", "1")  # if installed
    os.environ.setdefault("HF_HUB_DISABLE_TELEMETRY", "1")

    try:
        total, used, free = shutil.disk_usage("/models")
        log(f"💾 /models free: {human(free)} / total: {human(total)}")
    except Exception as e:
        log(f"⚠️ disk usage 확인 실패: {e}")

    if NAME == "llama3-instruct-kor-8b-q4km":
        if exists(NAME):
            log(f"✅ 이미 생성된 모델: {NAME}")
            return

        # ensure huggingface_hub
        try:
            subprocess.check_call([sys.executable, "-m", "pip", "show", "huggingface_hub"])
        except subprocess.CalledProcessError:
            subprocess.check_call([sys.executable, "-m", "pip", "install", "--no-cache-dir", "huggingface_hub==0.24.6"])

        from huggingface_hub import snapshot_download, login, HfApi
        token = os.environ.get("HF_TOKEN") or None
        if token:
            try:
                login(token=token); log("🔑 HF 토큰 로그인 성공")
            except Exception as e:
                log(f"⚠️ HF 토큰 로그인 경고: {e}")

        target_dir = "/models/llama3-instruct-kor-8b-q4km"
        repo_id    = "QuantFactory/Llama-3.1-Korean-8B-Instruct-GGUF"
        pattern    = "*Q4_K_M.gguf"
        os.makedirs(target_dir, exist_ok=True)

        log(f"📥 HF 다운로드 시작: {repo_id} → {target_dir} (pattern='{pattern}')")
        t0 = time.time()
        snapshot_download(
            repo_id=repo_id,
            local_dir=target_dir,
            allow_patterns=[pattern],
            local_dir_use_symlinks=False,
        )
        dt = time.time() - t0
        log(f"✅ HF 다운로드 완료 ({dt:.1f}s)")

        ggufs = glob.glob(os.path.join(target_dir, pattern))
        if not ggufs: raise RuntimeError("GGUF 파일을 찾을 수 없습니다.")
        gguf = ggufs[0]
        fname = os.path.basename(gguf)
        sz = os.path.getsize(gguf)
        log(f"📄 GGUF 파일: {gguf} ({human(sz)})")

        # --- blobs + create(files + modelfile) ---
        log("🔐 SHA256 계산 중...")
        digest = sha256_of_file(gguf)
        log(f"✅ SHA256: {digest}")

        if blob_exists(digest):
            log("ℹ️ blob 이미 존재 → 업로드 스킵")
        else:
            log("⬆️ blob 업로드 시작 (streaming)")
            upload_blob_with_digest(gguf, digest)
            log("✅ blob 업로드 완료")

        # /api/create 호출
        payload = {
            "model": NAME,
            "files": { fname: digest }  # ← 필수
        }

        log("🏗️ Ollama 모델 생성 시작 (/api/create)")
        stream_post("/api/create", payload)
        log(f"✅ 모델 생성 완료: {NAME}")

    else:
        if exists(NAME):
            log(f"✅ 이미 존재하는 모델(원격): {NAME}")
            return
        log(f"🌐 Ollama pull 시작: {NAME}")
        stream_post("/api/pull", {"model": NAME})
        log(f"✅ pull 완료: {NAME}")

if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        log(f"❌ 오류: {e}")
        sys.exit(1)
