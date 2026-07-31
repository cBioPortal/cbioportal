"""Small deterministic tile-server contract fixture for the local WSI CI job."""

import base64
import json
import os
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse


PORT = int(os.environ.get("SERVER_PORT", "8081"))
SERVABLE_SLIDES = {"3020726", "3020691", "openslide-small"}
UNMATCHED_SLIDE = "3020648"

# A valid 1x1 JPEG keeps the fixture self-contained and fast.
JPEG_BYTES = base64.b64decode(
    "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////2wBDAf//////////////////////////////////////////////////////////////////////////////////////wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAX/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIQAxAAAAH/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEBAAEFAqf/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oACAEDAQE/AYf/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oACAECAQE/AYf/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEBAAY/Aqf/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEBAAE/IV//2gAMAwEAAgADAAAAEP/EABQRAQAAAAAAAAAAAAAAAAAAABD/2gAIAQMBAT8QH//EABQRAQAAAAAAAAAAAAAAAAAAABD/2gAIAQIBAT8QH//EABQQAQAAAAAAAAAAAAAAAAAAABD/2gAIAQEAAT8QH//Z"
)

METADATA = {
    "dimensions": {"width": 256, "height": 256},
    "levels": 1,
    "level_dimensions": [{"width": 256, "height": 256}],
    "tile_size": 256,
    "objective_power": 20,
    "vendor": "aperio",
}


class Handler(BaseHTTPRequestHandler):
    def log_message(self, format, *args):
        return

    def send_bytes(self, body, content_type):
        self.send_response(200)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        path = urlparse(self.path).path
        parts = path.strip("/").split("/")
        if len(parts) < 3 or parts[0] != "tiles":
            self.send_error(404)
            return

        slide_id, resource = parts[1], parts[2]
        if slide_id == UNMATCHED_SLIDE or slide_id not in SERVABLE_SLIDES:
            self.send_error(404)
            return

        if resource == "metadata":
            self.send_bytes(json.dumps(METADATA).encode("utf-8"), "application/json")
        elif resource == "thumbnail" or resource == "zxy":
            self.send_bytes(JPEG_BYTES, "image/jpeg")
        else:
            self.send_error(404)


if __name__ == "__main__":
    ThreadingHTTPServer(("0.0.0.0", PORT), Handler).serve_forever()
