from __future__ import annotations

import base64
import sys
from pathlib import Path

from docx import Document
from lxml import etree


def decode_arg(value: str) -> Path:
    return Path(base64.b64decode(value).decode("utf-8"))


def remove_page_breaks(paragraph) -> None:
    ns = {"w": "http://schemas.openxmlformats.org/wordprocessingml/2006/main"}
    for br in list(paragraph._p.xpath(".//w:br[@w:type='page']", namespaces=ns)):
        parent = br.getparent()
        parent.remove(br)


def main() -> int:
    docx = decode_arg(sys.argv[1])
    doc = Document(str(docx))
    # The cover paragraph already ends with a section break. The following manual
    # page break creates an extra blank page before the Chinese abstract.
    remove_page_breaks(doc.paragraphs[19])
    doc.save(str(docx))
    print(docx)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
