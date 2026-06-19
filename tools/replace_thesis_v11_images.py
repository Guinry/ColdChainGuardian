from __future__ import annotations

import base64
import shutil
import sys
import tempfile
import zipfile
from pathlib import Path

from PIL import Image
from lxml import etree


EMU_PER_INCH = 914400


def decode_arg(value: str) -> Path:
    return Path(base64.b64decode(value).decode("utf-8"))


def main() -> int:
    docx = decode_arg(sys.argv[1])
    replacements = {
        "word/media/image4.png": decode_arg(sys.argv[2]),
        "word/media/image5.png": decode_arg(sys.argv[3]),
        "word/media/image6.png": decode_arg(sys.argv[4]),
        "word/media/image7.png": decode_arg(sys.argv[5]),
    }
    # Existing figure widths in inches, matched to v9/v11 original figure slots.
    desired_widths = {
        "word/media/image4.png": 5.59,
        "word/media/image5.png": 5.71,
        "word/media/image6.png": 5.59,
        "word/media/image7.png": 6.10,
    }

    ns = {
        "w": "http://schemas.openxmlformats.org/wordprocessingml/2006/main",
        "a": "http://schemas.openxmlformats.org/drawingml/2006/main",
        "r": "http://schemas.openxmlformats.org/officeDocument/2006/relationships",
        "wp": "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing",
    }

    with tempfile.TemporaryDirectory() as tmp:
        tmpdir = Path(tmp)
        with zipfile.ZipFile(docx, "r") as zin:
            zin.extractall(tmpdir)

        # Resolve rId -> media target.
        rels_path = tmpdir / "word" / "_rels" / "document.xml.rels"
        rels_tree = etree.parse(str(rels_path))
        relmap = {}
        for rel in rels_tree.getroot():
            rid = rel.get("Id")
            target = rel.get("Target")
            if rid and target and target.startswith("media/"):
                relmap[rid] = "word/" + target

        doc_path = tmpdir / "word" / "document.xml"
        tree = etree.parse(str(doc_path))
        root = tree.getroot()

        # Replace media and update extents to preserve generated image aspect ratios.
        for target, image_path in replacements.items():
            shutil.copyfile(image_path, tmpdir / target)
            with Image.open(image_path) as image:
                ratio = image.height / image.width
            width_emu = int(desired_widths[target] * EMU_PER_INCH)
            height_emu = int(width_emu * ratio)

            for blip in root.xpath(".//a:blip[@r:embed]", namespaces=ns):
                rid = blip.get(f"{{{ns['r']}}}embed")
                if relmap.get(rid) != target:
                    continue
                inline = blip.xpath("ancestor::wp:inline[1]", namespaces=ns)
                if not inline:
                    continue
                inline = inline[0]
                for extent in inline.xpath("./wp:extent", namespaces=ns):
                    extent.set("cx", str(width_emu))
                    extent.set("cy", str(height_emu))
                for ext in inline.xpath(".//a:ext", namespaces=ns):
                    ext.set("cx", str(width_emu))
                    ext.set("cy", str(height_emu))

        tree.write(str(doc_path), xml_declaration=True, encoding="UTF-8", standalone="yes")

        backup = docx.with_suffix(".before-image-replace.docx")
        if not backup.exists():
            shutil.copy2(docx, backup)
        with zipfile.ZipFile(docx, "w", compression=zipfile.ZIP_DEFLATED) as zout:
            for file in tmpdir.rglob("*"):
                if file.is_file():
                    zout.write(file, file.relative_to(tmpdir).as_posix())

    print(docx)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
