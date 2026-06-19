import argparse
import base64
import json
import os
import re
import sys
import urllib.error
import urllib.request
from pathlib import Path


def parse_args():
    parser = argparse.ArgumentParser(description="Generate an image through MicuAPI chat-completions image models.")
    parser.add_argument("--prompt", required=True, help="Image prompt.")
    parser.add_argument("--out", required=True, help="Output image path.")
    parser.add_argument("--model", default=os.environ.get("MICU_IMAGE_MODEL", "gpt-image-2"))
    parser.add_argument("--base-url", default=os.environ.get("MICU_API_BASE_URL", "https://www.micuapi.ai"))
    return parser.parse_args()


def request_json(url, api_key, payload):
    req = urllib.request.Request(
        url,
        data=json.dumps(payload, ensure_ascii=False).encode("utf-8"),
        headers={
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
        },
        method="POST",
    )
    with urllib.request.urlopen(req, timeout=300) as resp:
        return json.loads(resp.read().decode("utf-8", errors="replace"))


def find_image_payload(data):
    text_parts = []
    for choice in data.get("choices", []):
        message = choice.get("message") or {}
        content = message.get("content")
        if isinstance(content, str):
            text_parts.append(content)
        elif isinstance(content, list):
            for item in content:
                if isinstance(item, dict):
                    if item.get("type") == "image_url":
                        image_url = item.get("image_url")
                        if isinstance(image_url, dict) and image_url.get("url"):
                            return image_url["url"]
                        if isinstance(image_url, str):
                            return image_url
                    if item.get("type") in {"text", "output_text"} and item.get("text"):
                        text_parts.append(item["text"])

    raw = json.dumps(data, ensure_ascii=False)
    for candidate in text_parts + [raw]:
        match = re.search(r"https?://[^\s\"'<>\)\]\}]+", candidate)
        if match:
            return match.group(0).rstrip(".,;:")
        match = re.search(r"data:image/[^;]+;base64,([A-Za-z0-9+/=\s]+)", candidate)
        if match:
            return "data:image/png;base64," + match.group(1)
    return None


def save_image(image_payload, out_path, api_key):
    out_path.parent.mkdir(parents=True, exist_ok=True)
    if image_payload.startswith("data:image"):
        b64 = image_payload.split(",", 1)[1]
        out_path.write_bytes(base64.b64decode(b64))
        return
    req = urllib.request.Request(image_payload, headers={"User-Agent": "Mozilla/5.0", "Authorization": f"Bearer {api_key}"})
    try:
        with urllib.request.urlopen(req, timeout=180) as resp:
            out_path.write_bytes(resp.read())
    except urllib.error.HTTPError:
        req = urllib.request.Request(image_payload, headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req, timeout=180) as resp:
            out_path.write_bytes(resp.read())


def main():
    args = parse_args()
    api_key = os.environ.get("MICU_API_KEY")
    if not api_key:
        print("MICU_API_KEY is not configured.", file=sys.stderr)
        return 2

    payload = {
        "model": args.model,
        "messages": [
            {
                "role": "user",
                "content": args.prompt,
            }
        ],
    }
    url = args.base_url.rstrip("/") + "/v1/chat/completions"
    data = request_json(url, api_key, payload)
    image_payload = find_image_payload(data)
    if not image_payload:
        print(json.dumps(data, ensure_ascii=False, indent=2), file=sys.stderr)
        print("No image URL or base64 image found in chat completion response.", file=sys.stderr)
        return 1

    out_path = Path(args.out)
    save_image(image_payload, out_path, api_key)
    print(json.dumps({"ok": True, "model": args.model, "path": str(out_path), "bytes": out_path.stat().st_size}, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
