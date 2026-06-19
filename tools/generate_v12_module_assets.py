from __future__ import annotations

import math
import shutil
import zipfile
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFont


ROOT = Path(r"C:\Users\23869\Desktop\ColdChainGuardian")
V9_DOCX = Path(r"C:\Users\23869\Desktop\毕设\草稿\郭鑫瑞毕业论文v9.docx")
OUT = ROOT / "output" / "doc" / "qa_v12"
MEDIA = OUT / "v9_media" / "media"
DIAGRAMS = OUT / "module_diagrams"
SNAPS = OUT / "selected_screenshots"


def ensure_dirs() -> None:
    for path in (MEDIA, DIAGRAMS, SNAPS):
        path.mkdir(parents=True, exist_ok=True)


def extract_v9_media() -> None:
    ensure_dirs()
    if any(MEDIA.glob("image15.*")) and any(MEDIA.glob("image33.*")):
        return
    with zipfile.ZipFile(V9_DOCX) as zf:
        for info in zf.infolist():
            if info.filename.startswith("word/media/"):
                dest = MEDIA / Path(info.filename).name
                with zf.open(info) as src, open(dest, "wb") as dst:
                    shutil.copyfileobj(src, dst)


def font_path(bold: bool = False) -> str:
    candidates = [
        r"C:\Windows\Fonts\msyhbd.ttc" if bold else r"C:\Windows\Fonts\msyh.ttc",
        r"C:\Windows\Fonts\simhei.ttf",
        r"C:\Windows\Fonts\simsun.ttc",
        r"C:\Windows\Fonts\arial.ttf",
    ]
    for candidate in candidates:
        if candidate and Path(candidate).exists():
            return candidate
    raise FileNotFoundError("No available font")


def ft(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(font_path(bold), size=size)


def canvas(w: int, h: int) -> tuple[Image.Image, ImageDraw.ImageDraw]:
    img = Image.new("RGB", (w, h), "white")
    return img, ImageDraw.Draw(img)


def text_size(draw: ImageDraw.ImageDraw, text: str, font: ImageFont.FreeTypeFont) -> tuple[int, int]:
    box = draw.textbbox((0, 0), text, font=font)
    return box[2] - box[0], box[3] - box[1]


def fit_text_font(
    draw: ImageDraw.ImageDraw,
    lines: list[str],
    size: int,
    bold: bool,
    max_w: int,
    max_h: int,
) -> ImageFont.FreeTypeFont:
    for s in range(size, 28, -2):
        fnt = ft(s, bold)
        gap = max(5, s // 7)
        widths = [text_size(draw, line, fnt)[0] for line in lines]
        heights = [text_size(draw, line, fnt)[1] for line in lines]
        if widths and max(widths) <= max_w and sum(heights) + gap * (len(lines) - 1) <= max_h:
            return fnt
    return ft(30, bold)


def center_text(
    draw: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    text: str,
    size: int = 44,
    bold: bool = False,
) -> None:
    x1, y1, x2, y2 = xy
    lines = text.split("\n")
    fnt = fit_text_font(draw, lines, size, bold, x2 - x1 - 24, y2 - y1 - 18)
    gap = max(5, fnt.size // 7)
    heights = [text_size(draw, line, fnt)[1] for line in lines]
    total_h = sum(heights) + gap * (len(lines) - 1)
    y = y1 + (y2 - y1 - total_h) / 2
    for line, h in zip(lines, heights):
        w, _ = text_size(draw, line, fnt)
        draw.text((x1 + (x2 - x1 - w) / 2, y), line, fill="black", font=fnt)
        y += h + gap


def box(
    draw: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    text: str,
    size: int = 44,
    bold: bool = False,
    width: int = 5,
) -> None:
    draw.rounded_rectangle(xy, radius=10, outline="black", width=width, fill="white")
    center_text(draw, xy, text, size, bold)


def diamond(
    draw: ImageDraw.ImageDraw,
    cx: int,
    cy: int,
    w: int,
    h: int,
    text: str,
    size: int = 38,
) -> tuple[int, int, int, int]:
    pts = [(cx, cy - h // 2), (cx + w // 2, cy), (cx, cy + h // 2), (cx - w // 2, cy)]
    draw.polygon(pts, outline="black", fill="white")
    draw.line(pts + [pts[0]], fill="black", width=5)
    center_text(draw, (cx - w // 2 + 24, cy - h // 2 + 20, cx + w // 2 - 24, cy + h // 2 - 20), text, size)
    return (cx - w // 2, cy - h // 2, cx + w // 2, cy + h // 2)


def line(draw: ImageDraw.ImageDraw, pts: list[tuple[int, int]], width: int = 5) -> None:
    draw.line(pts, fill="black", width=width)


def arrow_head(draw: ImageDraw.ImageDraw, p1: tuple[int, int], p2: tuple[int, int], size: int = 24) -> None:
    angle = math.atan2(p2[1] - p1[1], p2[0] - p1[0])
    left = (p2[0] - size * math.cos(angle - math.pi / 6), p2[1] - size * math.sin(angle - math.pi / 6))
    right = (p2[0] - size * math.cos(angle + math.pi / 6), p2[1] - size * math.sin(angle + math.pi / 6))
    draw.polygon([p2, left, right], fill="black")


def arrow(draw: ImageDraw.ImageDraw, pts: list[tuple[int, int]], width: int = 5, head: int = 24) -> None:
    line(draw, pts, width)
    arrow_head(draw, pts[-2], pts[-1], head)


def dashed_line(draw: ImageDraw.ImageDraw, p1: tuple[int, int], p2: tuple[int, int], width: int = 3, dash: int = 24) -> None:
    x1, y1 = p1
    x2, y2 = p2
    length = math.hypot(x2 - x1, y2 - y1)
    if length == 0:
        return
    dx = (x2 - x1) / length
    dy = (y2 - y1) / length
    pos = 0
    draw_segment = True
    while pos < length:
        end = min(pos + dash, length)
        if draw_segment:
            draw.line([(x1 + dx * pos, y1 + dy * pos), (x1 + dx * end, y1 + dy * end)], fill="black", width=width)
        draw_segment = not draw_segment
        pos = end


def save_crop(img: Image.Image, filename: str, margin: int = 40) -> None:
    bg = Image.new("RGB", img.size, "white")
    diff = ImageChops.difference(img, bg)
    bbox = diff.getbbox()
    if bbox:
        x1, y1, x2, y2 = bbox
        img = img.crop((max(0, x1 - margin), max(0, y1 - margin), min(img.width, x2 + margin), min(img.height, y2 + margin)))
    img.save(DIAGRAMS / filename, quality=95, dpi=(300, 300))


def sequence_diagram(filename: str, labels: list[str], messages: list[tuple[int, int, int, str]], w: int = 1900, h: int = 900) -> None:
    img, draw = canvas(w, h)
    xs = [int(150 + i * ((w - 300) / (len(labels) - 1))) for i in range(len(labels))]
    for x, label in zip(xs, labels):
        box(draw, (x - 130, 60, x + 130, 135), label, 38)
        dashed_line(draw, (x, 135), (x, h - 95), width=3, dash=22)
    for a, b, y, msg in messages:
        arrow(draw, [(xs[a], y), (xs[b], y)], 4, 20)
        xy = (min(xs[a], xs[b]) + 12, y - 52, max(xs[a], xs[b]) - 12, y - 12)
        draw.rectangle(xy, fill="white")
        center_text(draw, xy, msg, 35)
    save_crop(img, filename)


def diagram_auth_dashboard() -> None:
    sequence_diagram(
        "fig5_01_auth_dashboard_sequence.png",
        ["用户", "登录页", "后端服务", "数据库", "Dashboard"],
        [
            (0, 1, 220, "输入账号密码"),
            (1, 2, 310, "提交登录请求"),
            (2, 3, 400, "查询用户角色"),
            (3, 2, 490, "返回认证数据"),
            (2, 1, 580, "返回 Token"),
            (1, 4, 670, "进入首页"),
            (4, 2, 760, "加载概览数据"),
            (2, 4, 840, "返回统计结果"),
        ],
        1900,
        930,
    )


def diagram_area_device() -> None:
    img, draw = canvas(1900, 820)
    nodes = [
        (80, 110, "选择库区"),
        (380, 110, "读取库区树"),
        (680, 110, "维护库区信息"),
        (980, 110, "绑定设备"),
        (1280, 110, "刷新页面"),
        (380, 450, "设备列表"),
        (680, 450, "新增或编辑"),
        (980, 450, "状态校验"),
        (1280, 450, "写入数据库"),
    ]
    for x, y, text in nodes:
        box(draw, (x, y, x + 235, y + 84), text, 40)
    for row in [nodes[:5], nodes[5:]]:
        for a, b in zip(row, row[1:]):
            arrow(draw, [(a[0] + 235, a[1] + 42), (b[0], b[1] + 42)], 5)
    arrow(draw, [(1098, 194), (1098, 345), (498, 345), (498, 450)], 5)
    arrow(draw, [(1398, 450), (1398, 300), (1398, 194)], 5)
    save_crop(img, "fig5_03_area_device_flow.png")


def diagram_monitor_threshold() -> None:
    img, draw = canvas(1900, 760)
    top = [(80, 100, "设备采集"), (380, 100, "数据接收"), (680, 100, "数据入库"), (980, 100, "规则匹配"), (1280, 100, "状态更新")]
    bottom = [(1280, 440, "分页表格"), (980, 440, "趋势图表"), (680, 440, "实时监测页"), (380, 440, "WebSocket")]
    for x, y, text in top + bottom:
        box(draw, (x, y, x + 235, y + 84), text, 40)
    for a, b in zip(top, top[1:]):
        arrow(draw, [(a[0] + 235, a[1] + 42), (b[0], b[1] + 42)], 5)
    arrow(draw, [(1398, 184), (1398, 440)], 5)
    for a, b in zip(bottom, bottom[1:]):
        arrow(draw, [(a[0], a[1] + 42), (b[0] + 235, b[1] + 42)], 5)
    arrow(draw, [(498, 440), (498, 300), (980, 300), (980, 184)], 5)
    box(draw, (80, 440, 315, 524), "阈值规则维护", 38)
    arrow(draw, [(315, 482), (380, 482)], 5)
    save_crop(img, "fig5_06_monitor_threshold_flow.png")


def diagram_alert_order() -> None:
    img, draw = canvas(2000, 800)
    upper = [(70, 115, "异常数据"), (360, 115, "生成告警"), (650, 115, "告警研判"), (940, 115, "派发工单"), (1230, 115, "员工接收"), (1520, 115, "现场处理")]
    lower = [(1520, 500, "提交反馈"), (1230, 500, "管理验收"), (940, 500, "关闭归档"), (650, 500, "退回处理"), (360, 500, "误报关闭")]
    for x, y, text in upper + lower:
        box(draw, (x, y, x + 220, y + 84), text, 39)
    for a, b in zip(upper, upper[1:]):
        arrow(draw, [(a[0] + 220, a[1] + 42), (b[0], b[1] + 42)], 5)
    arrow(draw, [(1630, 199), (1630, 500)], 5)
    for a, b in zip(lower, lower[1:3]):
        arrow(draw, [(a[0], a[1] + 42), (b[0] + 220, b[1] + 42)], 5)
    arrow(draw, [(1230, 542), (1160, 542), (1160, 650), (760, 650), (760, 584)], 5)
    arrow(draw, [(650, 157), (470, 157), (470, 500)], 5)
    arrow(draw, [(650, 542), (580, 542), (580, 320), (1630, 320), (1630, 199)], 5)
    save_crop(img, "fig5_09_alert_order_flow.png")


def diagram_analysis_system() -> None:
    img, draw = canvas(1900, 780)
    nodes = [
        (80, 105, "选择时间范围"),
        (380, 105, "聚合监测数据"),
        (680, 105, "生成指标卡片"),
        (980, 105, "绘制趋势图"),
        (1280, 105, "形成报表"),
        (380, 455, "角色校验"),
        (680, 455, "用户与权限"),
        (980, 455, "操作日志"),
        (1280, 455, "审计追踪"),
    ]
    for x, y, text in nodes:
        box(draw, (x, y, x + 235, y + 84), text, 40)
    for row in [nodes[:5], nodes[5:]]:
        for a, b in zip(row, row[1:]):
            arrow(draw, [(a[0] + 235, a[1] + 42), (b[0], b[1] + 42)], 5)
    arrow(draw, [(1398, 189), (1398, 455)], 5)
    arrow(draw, [(498, 455), (498, 300), (198, 300), (198, 189)], 5)
    save_crop(img, "fig5_12_analysis_system_flow.png")


def diagram_ai_assistant() -> None:
    sequence_diagram(
        "fig5_15_ai_assistant_sequence.png",
        ["用户", "AI 助手页", "后端服务", "业务数据库", "大模型接口"],
        [
            (0, 1, 220, "输入业务问题"),
            (1, 2, 320, "提交会话请求"),
            (2, 3, 420, "查询监测告警数据"),
            (3, 2, 520, "返回业务上下文"),
            (2, 4, 620, "调用模型接口"),
            (4, 2, 720, "返回分析结果"),
            (2, 1, 820, "渲染格式化答复"),
        ],
        1900,
        900,
    )


def diagram_mini_order() -> None:
    img, draw = canvas(2000, 720)
    top = [(70, 90, "小程序登录"), (390, 90, "工作台"), (710, 90, "告警大厅"), (1030, 90, "接收工单"), (1350, 90, "现场处理")]
    lower = [(1350, 450, "提交反馈"), (1030, 450, "待验收"), (710, 450, "验收通过"), (390, 450, "已完成"), (70, 450, "退回处理")]
    for x, y, text in top + lower:
        box(draw, (x, y, x + 250, y + 84), text, 40)
    for a, b in zip(top, top[1:]):
        arrow(draw, [(a[0] + 250, a[1] + 42), (b[0], b[1] + 42)], 5)
    arrow(draw, [(1475, 174), (1475, 450)], 5)
    for a, b in zip(lower, lower[1:4]):
        arrow(draw, [(a[0], a[1] + 42), (b[0] + 250, b[1] + 42)], 5)
    arrow(draw, [(1030, 534), (1030, 610), (195, 610), (195, 534)], 5)
    arrow(draw, [(320, 492), (390, 492)], 5)
    save_crop(img, "fig5_18_mini_program_flow.png")


def copy_screenshot(image_no: int, name: str) -> None:
    src = MEDIA / f"image{image_no}.png"
    if not src.exists():
        raise FileNotFoundError(src)
    shutil.copy2(src, SNAPS / name)


def screenshot_collage(items: list[tuple[int, str]], name: str, columns: int = 2, cell_w: int = 620) -> None:
    images: list[tuple[str, Image.Image]] = []
    for no, label in items:
        im = Image.open(MEDIA / f"image{no}.png").convert("RGB")
        ratio = im.height / im.width
        th = int(cell_w * ratio)
        images.append((label, im.resize((cell_w, th), Image.Resampling.LANCZOS)))
    pad = 34
    label_h = 42
    row_h = max(im.height for _, im in images) + label_h + pad
    rows = math.ceil(len(images) / columns)
    sheet = Image.new("RGB", (columns * cell_w + (columns + 1) * pad, rows * row_h + pad), "white")
    draw = ImageDraw.Draw(sheet)
    label_font = ft(28)
    for idx, (label, im) in enumerate(images):
        x = pad + (idx % columns) * (cell_w + pad)
        y = pad + (idx // columns) * row_h
        sheet.paste(im, (x, y))
        center_text(draw, (x, y + im.height + 6, x + cell_w, y + im.height + label_h), label, 28)
    sheet.save(SNAPS / name, quality=95, dpi=(220, 220))


def prepare_screenshots() -> None:
    copy_screenshot(15, "dashboard.png")
    copy_screenshot(16, "area_management.png")
    copy_screenshot(17, "device_management.png")
    copy_screenshot(18, "threshold_rules.png")
    copy_screenshot(19, "realtime_monitor.png")
    copy_screenshot(20, "alert_center.png")
    copy_screenshot(21, "work_order_center.png")
    copy_screenshot(22, "data_analysis.png")
    copy_screenshot(23, "system_permission.png")
    copy_screenshot(24, "ai_assistant.png")
    screenshot_collage(
        [(25, "工作台"), (26, "告警未处理"), (29, "待接收工单"), (30, "处理中工单")],
        "mini_workbench_alert_order.png",
        columns=4,
        cell_w=330,
    )
    screenshot_collage(
        [(31, "待验收工单"), (32, "已完成工单"), (33, "个人中心")],
        "mini_verify_profile.png",
        columns=3,
        cell_w=360,
    )


def main() -> None:
    ensure_dirs()
    extract_v9_media()
    diagram_auth_dashboard()
    diagram_area_device()
    diagram_monitor_threshold()
    diagram_alert_order()
    diagram_analysis_system()
    diagram_ai_assistant()
    diagram_mini_order()
    prepare_screenshots()
    print(DIAGRAMS)
    print(SNAPS)


if __name__ == "__main__":
    main()
