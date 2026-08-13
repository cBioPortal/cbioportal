"""Small deterministic tile-server contract fixture for the local WSI CI job."""

import base64
import hashlib
import hmac
import json
import os
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import parse_qs, urlparse


PORT = int(os.environ.get("SERVER_PORT", "8081"))
AUTH_SECRET = os.environ.get("WSI_AUTH_SECRET", "")
AUTH_AUDIENCE = os.environ.get("WSI_AUTH_AUDIENCE", "cbioportal-wsi")
SERVABLE_SLIDES = {"3020726", "3020691", "openslide-small"}

# A valid 1x1 JPEG keeps the fixture self-contained and fast.
JPEG_BYTES = base64.b64decode(
    "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////2wBDAf//////////////////////////////////////////////////////////////////////////////////////wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAX/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIQAxAAAAH/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEBAAEFAqf/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oACAEDAQE/AYf/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oACAECAQE/AYf/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEBAAY/Aqf/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEBAAE/IV//2gAMAwEAAgADAAAAEP/EABQRAQAAAAAAAAAAAAAAAAAAABD/2gAIAQMBAT8QH//EABQRAQAAAAAAAAAAAAAAAAAAABD/2gAIAQIBAT8QH//EABQQAQAAAAAAAAAAAAAAAAAAABD/2gAIAQEAAT8QH//Z"
)

class Handler(BaseHTTPRequestHandler):
    def log_message(self, format, *args):
        return

    def send_bytes(self, body, content_type):
        self.send_response(200)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def authorize(self, source, kind):
        header = self.headers.get("Authorization", "")
        if not header.startswith("Bearer "):
            self.send_response(401)
            self.end_headers()
            return None
        try:
            encoded_header, encoded_payload, encoded_signature = header[7:].split(".")
            signing_input = f"{encoded_header}.{encoded_payload}".encode()
            expected = hmac.new(AUTH_SECRET.encode(), signing_input, hashlib.sha256).digest()
            actual = base64.urlsafe_b64decode(encoded_signature + "=" * (-len(encoded_signature) % 4))
            claims = json.loads(
                base64.urlsafe_b64decode(encoded_payload + "=" * (-len(encoded_payload) % 4))
            )
            if not hmac.compare_digest(expected, actual):
                raise ValueError
            if claims.get("aud") != AUTH_AUDIENCE or claims.get("scope") != "wsi:read":
                raise ValueError
            now = int(time.time())
            if not isinstance(claims.get("iat"), int) or not isinstance(claims.get("exp"), int):
                raise ValueError
            if claims["exp"] <= now or claims["iat"] > now + 60 or claims["exp"] - claims["iat"] > 300:
                raise ValueError
            if claims.get("wsi_auth_version") != 2:
                self.send_response(403)
                self.end_headers()
                return None
            if claims.get("image_id") not in SERVABLE_SLIDES:
                self.send_response(404)
                self.end_headers()
                return None
            claim_name = "thumbnail_source_sha256" if kind == "thumbnail" else "tile_source_sha256"
            if not hmac.compare_digest(hashlib.sha256(source.encode()).hexdigest(), claims.get(claim_name, "")):
                self.send_response(403)
                self.end_headers()
                return None
            return claims
        except Exception:
            self.send_response(401)
            self.end_headers()
            return None

    def do_GET(self):
        parsed = urlparse(self.path)
        path = parsed.path
        query = {key: values[0] for key, values in parse_qs(parsed.query).items()}
        parts = path.strip("/").split("/")
        source = query.get("source", "")
        if parts[:2] == ["tiles", "zxy"] and len(parts) == 5:
            if source and self.authorize(source, "tile") is not None:
                self.send_bytes(JPEG_BYTES, "image/jpeg")
            return
        if parts == ["thumbnails"]:
            if source and self.authorize(source, "thumbnail") is not None:
                self.send_bytes(JPEG_BYTES, "image/jpeg")
            return
        self.send_error(404)


if __name__ == "__main__":
    ThreadingHTTPServer(("0.0.0.0", PORT), Handler).serve_forever()
