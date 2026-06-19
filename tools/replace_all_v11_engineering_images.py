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
    diagram_dir = decode_arg(sys.argv[2])
    mapping = {
        "word/media/image1.png": "image1_roles_permissions.png",
        "word/media/image2.png": "image2_business_flow.png",
        "word/media/image3.png": "image3_alert_loop.png",
        "word/media/image4.png": "image4_use_case.png",
        "word/media/image5.png": "image5_architecture.png",
        "word/media/image6.png": "image6_deployment.png",
        "word/media/image7.png": "image7_modules.png",
        "word/media/image8.png": "image8_er.png",
        "word/media/image9.png": "image9_class.png",
        "word/media/image10.png": "image10_login_sequence.png",
        "word/media/image11.png": "image11_realtime_flow.png",
        "word/media/image12.png": "image12_alert_generation.png",
        "word/media/image13.png": "image13_ai_sequence.png",
        "word/media/image14.png": "image14_mini_order.png",
    }
    desired_widths = {
        "word/media/image1.png": 5.9,
        "word/media/image2.png": 5.9,
        "word/media/image3.png": 5.9,
        "word/media/image4.png": 5.9,
        "word/media/image5.png": 5.9,
        "word/media/image6.png": 5.9,
        "word/media/image7.png": 6.1,
        "word/media/image8.png": 5.9,
        "word/media/image9.png": 5.9,
        "word/media/image10.png": 5.9,
        "word/media/image11.png": 5.9,
        "word/media/image12.png": 5.9,
        "word/media/image13.png": 5.9,
        "word/media/image14.png": 5.9,
    }

    ns = {
        "a": "http://schemas.openxmlformats.org/drawingml/2006/main",
        "r": "http://schemas.openxmlformats.org/officeDocument/2006/relationships",
        "wp": "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing",
    }

    with tempfile.TemporaryDirectory() as tmp:
        tmpdir = Path(tmp)
        with zipfile.ZipFile(docx, "r") as zin:
            zin.extractall(tmpdir)

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

        for target, filename in mapping.items():
            image_path = diagram_dir / filename
            shutil.copyfile(image_path, tmpdir / target)
            with Image.open(image_path) as im:
                ratio = im.height / im.width
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

        backup = docx.with_suffix(".before-all-engineering-images.docx")
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
