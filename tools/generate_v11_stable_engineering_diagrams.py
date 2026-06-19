from __future__ import annotations

import math
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFont


OUT = Path(r"C:\Users\23869\Desktop\ColdChainGuardian\output\doc\qa_v11\diagrams_stable")
OUT.mkdir(parents=True, exist_ok=True)


def font_path(bold: bool = False) -> str:
    candidates = [
        r"C:\Windows\Fonts\msyhbd.ttc" if bold else r"C:\Windows\Fonts\msyh.ttc",
        r"C:\Windows\Fonts\simhei.ttf",
        r"C:\Windows\Fonts\simsun.ttc",
        r"C:\Windows\Fonts\arial.ttf",
    ]
    for path in candidates:
        if path and Path(path).exists():
            return path
    raise FileNotFoundError("No usable font found")


def f(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(font_path(bold), size=size)


def canvas(w: int, h: int) -> tuple[Image.Image, ImageDraw.ImageDraw]:
    img = Image.new("RGB", (w, h), "white")
    return img, ImageDraw.Draw(img)


def text_wh(d: ImageDraw.ImageDraw, text: str, ft: ImageFont.FreeTypeFont) -> tuple[int, int]:
    bbox = d.textbbox((0, 0), text, font=ft)
    return bbox[2] - bbox[0], bbox[3] - bbox[1]


def split_cn(text: str, max_chars: int) -> list[str]:
    if "\n" in text:
        return text.split("\n")
    if len(text) <= max_chars:
        return [text]
    lines: list[str] = []
    s = text
    while s:
        lines.append(s[:max_chars])
        s = s[max_chars:]
    return lines


def fit_font(
    d: ImageDraw.ImageDraw,
    lines: list[str],
    size: int,
    bold: bool,
    max_w: int,
    max_h: int,
    min_size: int = 30,
) -> ImageFont.FreeTypeFont:
    for s in range(size, min_size - 1, -2):
        ft = f(s, bold)
        widths = [text_wh(d, line, ft)[0] for line in lines]
        heights = [text_wh(d, line, ft)[1] for line in lines]
        total_h = sum(heights) + max(0, len(lines) - 1) * max(5, s // 7)
        if (not widths or max(widths) <= max_w) and total_h <= max_h:
            return ft
    return f(min_size, bold)


def center_text(
    d: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    text: str,
    size: int,
    bold: bool = False,
    max_chars: int | None = None,
) -> None:
    x1, y1, x2, y2 = xy
    if max_chars:
        lines = split_cn(text, max_chars)
    else:
        lines = text.split("\n")
    ft = fit_font(d, lines, size, bold, x2 - x1 - 28, y2 - y1 - 20)
    gap = max(6, ft.size // 7)
    heights = [text_wh(d, line, ft)[1] for line in lines]
    total_h = sum(heights) + gap * (len(lines) - 1)
    y = y1 + (y2 - y1 - total_h) / 2
    for line, h in zip(lines, heights):
        w, _ = text_wh(d, line, ft)
        d.text((x1 + (x2 - x1 - w) / 2, y), line, font=ft, fill="black")
        y += h + gap


def left_lines(
    d: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    lines: list[str],
    size: int = 44,
) -> None:
    x1, y1, x2, _y2 = xy
    ft = f(size)
    y = y1 + 26
    for line in lines:
        d.text((x1 + 28, y), line, font=ft, fill="black")
        y += text_wh(d, line, ft)[1] + 22


def rect(
    d: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    text: str,
    size: int = 54,
    bold: bool = False,
    width: int = 5,
    radius: int = 12,
    max_chars: int | None = None,
) -> None:
    d.rounded_rectangle(xy, radius=radius, outline="black", width=width, fill="white")
    center_text(d, xy, text, size, bold, max_chars=max_chars)


def plain_rect(d: ImageDraw.ImageDraw, xy: tuple[int, int, int, int], width: int = 5) -> None:
    d.rectangle(xy, outline="black", width=width, fill="white")


def ellipse(
    d: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    text: str,
    size: int = 52,
    width: int = 5,
) -> None:
    d.ellipse(xy, outline="black", width=width, fill="white")
    center_text(d, xy, text, size)


def diamond(
    d: ImageDraw.ImageDraw,
    cx: int,
    cy: int,
    w: int,
    h: int,
    text: str,
    size: int = 48,
) -> tuple[int, int, int, int]:
    pts = [(cx, cy - h // 2), (cx + w // 2, cy), (cx, cy + h // 2), (cx - w // 2, cy)]
    d.polygon(pts, outline="black", fill="white")
    d.line(pts + [pts[0]], fill="black", width=5)
    center_text(d, (cx - w // 2 + 24, cy - h // 2 + 22, cx + w // 2 - 24, cy + h // 2 - 22), text, size)
    return (cx - w // 2, cy - h // 2, cx + w // 2, cy + h // 2)


def line(d: ImageDraw.ImageDraw, pts: list[tuple[int, int]], width: int = 5) -> None:
    d.line(pts, fill="black", width=width)


def arrow_head(d: ImageDraw.ImageDraw, p1: tuple[int, int], p2: tuple[int, int], size: int = 24) -> None:
    angle = math.atan2(p2[1] - p1[1], p2[0] - p1[0])
    left = (p2[0] - size * math.cos(angle - math.pi / 6), p2[1] - size * math.sin(angle - math.pi / 6))
    right = (p2[0] - size * math.cos(angle + math.pi / 6), p2[1] - size * math.sin(angle + math.pi / 6))
    d.polygon([p2, left, right], fill="black")


def arrow(d: ImageDraw.ImageDraw, pts: list[tuple[int, int]], width: int = 5, head: int = 24) -> None:
    line(d, pts, width)
    arrow_head(d, pts[-2], pts[-1], head)


def dashed_line(
    d: ImageDraw.ImageDraw,
    p1: tuple[int, int],
    p2: tuple[int, int],
    width: int = 4,
    dash: int = 24,
) -> None:
    x1, y1 = p1
    x2, y2 = p2
    length = math.hypot(x2 - x1, y2 - y1)
    if length == 0:
        return
    dx = (x2 - x1) / length
    dy = (y2 - y1) / length
    pos = 0
    draw = True
    while pos < length:
        end = min(pos + dash, length)
        if draw:
            d.line([(x1 + dx * pos, y1 + dy * pos), (x1 + dx * end, y1 + dy * end)], fill="black", width=width)
        draw = not draw
        pos = end


def dashed_rect(d: ImageDraw.ImageDraw, xy: tuple[int, int, int, int], width: int = 5, dash: int = 30) -> None:
    x1, y1, x2, y2 = xy
    dashed_line(d, (x1, y1), (x2, y1), width, dash)
    dashed_line(d, (x2, y1), (x2, y2), width, dash)
    dashed_line(d, (x2, y2), (x1, y2), width, dash)
    dashed_line(d, (x1, y2), (x1, y1), width, dash)


def actor(d: ImageDraw.ImageDraw, cx: int, cy: int, text: str) -> None:
    d.ellipse((cx - 24, cy - 90, cx + 24, cy - 42), outline="black", width=5)
    d.line([(cx, cy - 42), (cx, cy + 40)], fill="black", width=5)
    d.line([(cx - 56, cy - 2), (cx + 56, cy - 2)], fill="black", width=5)
    d.line([(cx, cy + 40), (cx - 52, cy + 108)], fill="black", width=5)
    d.line([(cx, cy + 40), (cx + 52, cy + 108)], fill="black", width=5)
    center_text(d, (cx - 145, cy + 118, cx + 145, cy + 178), text, 42)


def save_crop(img: Image.Image, filename: str, margin: int = 50) -> None:
    bg = Image.new("RGB", img.size, "white")
    diff = ImageChops.difference(img, bg)
    bbox = diff.getbbox()
    if bbox:
        x1, y1, x2, y2 = bbox
        img = img.crop((max(0, x1 - margin), max(0, y1 - margin), min(img.width, x2 + margin), min(img.height, y2 + margin)))
    img.save(OUT / filename, quality=95, dpi=(300, 300))


def diagram_roles_permissions() -> None:
    img, d = canvas(1900, 900)
    rows = [
        (90, "系统管理员", ["用户与角色", "系统配置", "全局数据"]),
        (285, "仓储管理员", ["库区设备", "监测告警", "工单管理"]),
        (480, "现场员工", ["移动工单", "处置反馈", "个人信息"]),
        (675, "大模型服务", ["数据查询", "风险分析", "辅助建议"]),
    ]
    for y, role, scopes in rows:
        rect(d, (80, y, 390, y + 95), role, 52, True)
        group = (520, y - 18, 1780, y + 113)
        line(d, [(390, y + 48), (520, y + 48)], 5)
        plain_rect(d, group, 4)
        xs = [585, 950, 1315]
        for x, scope in zip(xs, scopes):
            rect(d, (x, y + 18, x + 285, y + 78), scope, 44, False, 4)
    save_crop(img, "image1_roles_permissions.png")


def diagram_business_flow() -> None:
    img, d = canvas(2050, 940)
    top = [(80, 95, "设备采集"), (435, 95, "数据接入"), (790, 95, "阈值判断"), (1145, 95, "告警生成"), (1500, 95, "实时推送")]
    mid = [(270, 405, "告警研判"), (625, 405, "工单派发"), (980, 405, "现场处理"), (1335, 405, "验收关闭")]
    bot = [(270, 715, "闭环记录"), (625, 715, "趋势统计"), (980, 715, "风险分析"), (1335, 715, "复盘优化")]
    for row in [top, mid, bot]:
        boxes = []
        for x, y, t in row:
            rect(d, (x, y, x + 240, y + 80), t, 46)
            boxes.append((x, y, x + 240, y + 80))
        for a, b in zip(boxes, boxes[1:]):
            arrow(d, [(a[2], (a[1] + a[3]) // 2), (b[0], (b[1] + b[3]) // 2)], 5)
    arrow(d, [(1740, 135), (1940, 135), (1940, 300), (390, 300), (390, 405)], 5)
    for a, b in zip([(390, 485), (745, 485), (1100, 485), (1455, 485)], [(390, 715), (745, 715), (1100, 715), (1455, 715)]):
        arrow(d, [a, b], 5)
    save_crop(img, "image2_business_flow.png")


def diagram_alert_loop() -> None:
    img, d = canvas(2150, 770)
    top = [(80, 120, "告警生成"), (380, 120, "管理研判"), (680, 120, "转为工单"), (980, 120, "员工接收"), (1280, 120, "现场处理"), (1580, 120, "管理验收"), (1880, 120, "闭环归档")]
    lower = [(380, 500, "误报关闭"), (680, 500, "直接解决"), (1280, 500, "退回处理")]
    for x, y, t in top + lower:
        rect(d, (x, y, x + 220, y + 84), t, 44)
    for a, b in zip(top, top[1:]):
        arrow(d, [(a[0] + 220, 162), (b[0], 162)], 5)
    arrow(d, [(490, 204), (490, 500)], 5)
    arrow(d, [(790, 204), (790, 500)], 5)
    arrow(d, [(1690, 204), (1690, 410), (1390, 410), (1390, 500)], 5)
    arrow(d, [(1280, 542), (1130, 542), (1130, 300), (1390, 300), (1390, 204)], 5)
    save_crop(img, "image3_alert_loop.png")


def diagram_use_case() -> None:
    img, d = canvas(2150, 990)
    dashed_rect(d, (410, 70, 1740, 900), 5, 34)
    center_text(d, (460, 95, 725, 150), "系统边界", 42)
    cases = {
        "login": (620, 170, 980, 260, "登录认证"),
        "system": (620, 310, 980, 400, "系统管理"),
        "area": (620, 450, 980, 540, "库区设备"),
        "monitor": (620, 590, 980, 680, "实时监测"),
        "rule": (620, 730, 980, 820, "阈值规则"),
        "alert": (1160, 240, 1520, 330, "告警处理"),
        "order": (1160, 410, 1520, 500, "工单闭环"),
        "analysis": (1160, 580, 1520, 670, "数据分析"),
        "ai": (1160, 750, 1520, 840, "AI 智能问答"),
    }
    # Draw associations first, then use cases cover line ends.
    for y in [215, 355]:
        line(d, [(250, 260), (410, 260), (410, y), (620, y)], 4)
    for y in [495, 635, 775]:
        line(d, [(250, 710), (410, 710), (410, y), (620, y)], 4)
    for y in [285, 455]:
        line(d, [(1520, y), (1740, y), (1740, 480), (1900, 480)], 4)
    line(d, [(1520, 795), (1740, 795), (1740, 800), (1900, 800)], 4)
    line(d, [(980, 495), (1080, 495), (1080, 625), (1160, 625)], 4)
    line(d, [(980, 635), (1080, 635), (1080, 285), (1160, 285)], 4)
    line(d, [(1340, 330), (1340, 410)], 4)
    actor(d, 150, 260, "系统管理员")
    actor(d, 150, 710, "仓储管理员")
    actor(d, 2000, 480, "现场员工")
    actor(d, 2000, 800, "大模型服务")
    for xy in cases.values():
        ellipse(d, xy[:4], xy[4], 48)
    save_crop(img, "image4_use_case.png")


def diagram_architecture() -> None:
    img, d = canvas(2000, 820)
    rows = [
        (70, "表现层", ["Web 管理端", "微信小程序端", "PC 浏览器"]),
        (250, "接入层", ["REST API", "JWT 认证", "WebSocket 推送"]),
        (430, "业务层", ["库区设备", "实时监测", "告警工单", "数据分析", "AI 助手"]),
        (610, "数据层", ["MySQL", "Redis", "日志审计", "大模型接口"]),
    ]
    for y, layer, nodes in rows:
        plain_rect(d, (70, y, 1930, y + 120), 5)
        center_text(d, (95, y + 22, 285, y + 98), layer, 50, True)
        gap = 34
        x0 = 350
        w = (1860 - x0 - gap * (len(nodes) - 1)) // len(nodes)
        for i, node in enumerate(nodes):
            x = x0 + i * (w + gap)
            rect(d, (x, y + 26, x + w, y + 94), node, 46, False, 4)
    for x in [650, 1000, 1350]:
        arrow(d, [(x, 190), (x, 250)], 5)
        arrow(d, [(x, 370), (x, 430)], 5)
        arrow(d, [(x, 550), (x, 610)], 5)
    save_crop(img, "image5_architecture.png")


def diagram_deployment() -> None:
    img, d = canvas(2050, 900)
    groups = [
        (70, 80, 430, 800, "客户端"),
        (560, 80, 1050, 800, "应用服务"),
        (1180, 80, 1570, 800, "数据服务"),
        (1710, 80, 1980, 800, "外部服务"),
    ]
    for x1, y1, x2, y2, title in groups:
        dashed_rect(d, (x1, y1, x2, y2), 5, 30)
        center_text(d, (x1 + 30, y1 + 18, x2 - 30, y1 + 78), title, 48, True)
    nodes = {
        "web": (120, 230, 380, 335, "Web\n管理端"),
        "mini": (120, 545, 380, 650, "微信\n小程序端"),
        "boot": (635, 220, 975, 345, "Spring Boot\n后端 8080"),
        "ws": (635, 525, 975, 650, "WebSocket\n服务"),
        "ctx": (635, 670, 975, 775, "AI 上下文\n构造"),
        "mysql": (1245, 220, 1505, 345, "MySQL\n数据库"),
        "redis": (1245, 525, 1505, 650, "Redis\n缓存"),
        "log": (1245, 670, 1505, 775, "文件与日志"),
        "llm": (1745, 370, 1950, 555, "兼容式\n大模型接口\n模型服务"),
    }
    for key, target in [("web", "boot"), ("mini", "ws"), ("boot", "mysql"), ("ws", "redis"), ("ctx", "log")]:
        a, b = nodes[key], nodes[target]
        arrow(d, [(a[2], (a[1] + a[3]) // 2), (b[0], (b[1] + b[3]) // 2)], 5)
    arrow(d, [(975, 722), (1110, 722), (1110, 835), (1660, 835), (1660, 462), (1745, 462)], 5)
    for b in nodes.values():
        rect(d, b[:4], b[4], 46)
    save_crop(img, "image6_deployment.png")


def diagram_modules() -> None:
    img, d = canvas(2000, 900)
    rect(d, (640, 45, 1360, 125), "冷链仓储安全管理系统", 48, True)
    cols = [
        (70, "Web 管理端", ["Dashboard 与检索", "库区设备维护", "实时监测与规则", "告警工单闭环", "系统管理与助手"]),
        (545, "微信小程序端", ["登录认证与工作台", "告警查看与提醒", "工单接收与处置", "反馈提交与个人中心"]),
        (1020, "后端服务", ["认证授权", "业务接口", "实时推送", "数据持久化", "日志审计"]),
        (1495, "智能分析", ["上下文构造", "数据摘要", "风险解释", "处置建议"]),
    ]
    line(d, [(1000, 125), (1000, 205), (310, 205), (1735, 205)], 5)
    for x, title, items in cols:
        arrow(d, [(x + 205, 205), (x + 205, 280)], 5)
        rect(d, (x, 280, x + 410, 355), title, 46, True)
        plain_rect(d, (x, 355, x + 410, 785), 5)
        left_lines(d, (x, 355, x + 410, 785), items, 39)
    save_crop(img, "image7_modules.png")


def rel_box(d: ImageDraw.ImageDraw, xy: tuple[int, int, int, int], text: str) -> None:
    rect(d, xy, text, 42, False, 4, 8)


def diagram_er() -> None:
    img, d = canvas(2000, 900)
    nodes = {
        "user": (80, 75, 340, 165, "用户"),
        "role": (520, 75, 780, 165, "角色"),
        "rule": (1200, 75, 1490, 165, "阈值规则"),
        "area": (80, 375, 340, 465, "库区"),
        "device": (520, 375, 780, 465, "设备"),
        "data": (955, 375, 1270, 465, "监测数据"),
        "alert": (1470, 375, 1730, 465, "告警"),
        "ai": (520, 680, 780, 770, "AI 会话"),
        "msg": (955, 680, 1270, 770, "AI 消息"),
        "order": (1470, 680, 1730, 770, "工单"),
    }
    rels = [
        ((340, 120), (520, 120), (398, 82, 458, 154), "N:1"),
        ((340, 420), (520, 420), (398, 382, 458, 454), "1:N"),
        ((780, 420), (955, 420), (840, 382, 900, 454), "1:N"),
        ((1270, 420), (1470, 420), (1345, 382, 1415, 454), "触发"),
        ((1600, 465), (1600, 680), (1565, 552, 1635, 624), "1:1"),
        ((780, 725), (955, 725), (840, 687, 900, 759), "1:N"),
        ((210, 165), (210, 725), (365, 725), (398, 687, 458, 759), "1:N"),
        ((1345, 165), (1345, 265), (1600, 265), (1600, 375), (1450, 232, 1522, 304), "匹配"),
    ]
    for rel in rels:
        *pts, lb, text = rel
        # Split the line at the label box so relation text never sits on a line.
        if len(pts) == 2:
            p1, p2 = pts
            if p1[1] == p2[1]:
                y = p1[1]
                line(d, [p1, (lb[0], y)], 5)
                line(d, [(lb[2], y), p2], 5)
            else:
                x = p1[0]
                line(d, [p1, (x, lb[1])], 5)
                line(d, [(x, lb[3]), p2], 5)
        elif len(pts) == 3:
            p1, p2, p3 = pts
            line(d, [p1, p2, (lb[0], p2[1])], 5)
            line(d, [(lb[2], p2[1]), p3], 5)
        else:
            p1, p2, p3, p4 = pts
            line(d, [p1, p2, (lb[0], p2[1])], 5)
            line(d, [(lb[2], p2[1]), p3, p4], 5)
        rel_box(d, lb, text)
    for b in nodes.values():
        rect(d, b[:4], b[4], 58, True)
    save_crop(img, "image8_er.png")


def diagram_class() -> None:
    img, d = canvas(1980, 900)
    nodes = {
        "user": (70, 80, 340, 180, "User\nlogin()"),
        "area": (470, 80, 740, 180, "Area\nbuildTree()"),
        "device": (870, 80, 1140, 180, "Device\nbindArea()"),
        "data": (1270, 80, 1580, 180, "SensorData\nsave()"),
        "rule": (470, 385, 740, 485, "Rule\nmatch()"),
        "alert": (870, 385, 1140, 485, "Alert\nconfirm()"),
        "order": (1270, 385, 1580, 485, "WorkOrder\ndispatch()"),
        "session": (470, 690, 740, 790, "AiSession\ncreate()"),
        "message": (870, 690, 1140, 790, "AiMessage\nappend()"),
    }
    # Draw links first; class boxes cover endpoints cleanly.
    line(d, [(340, 130), (470, 130)], 4)
    line(d, [(740, 130), (870, 130)], 4)
    line(d, [(1140, 130), (1270, 130)], 4)
    line(d, [(1005, 180), (1005, 385)], 4)
    line(d, [(740, 435), (870, 435)], 4)
    line(d, [(1140, 435), (1270, 435)], 4)
    line(d, [(205, 180), (205, 740), (470, 740)], 4)
    line(d, [(740, 740), (870, 740)], 4)
    for b in nodes.values():
        rect(d, b[:4], b[4], 48, True)
    save_crop(img, "image9_class.png")


def sequence_diagram(filename: str, labels: list[str], messages: list[tuple[int, int, int, str]], w: int, h: int) -> None:
    img, d = canvas(w, h)
    count = len(labels)
    xs = [int(170 + i * ((w - 340) / (count - 1))) for i in range(count)]
    for x, title in zip(xs, labels):
        rect(d, (x - 130, 65, x + 130, 145), title, 42)
        dashed_line(d, (x, 140), (x, h - 105), 3, 22)
    for a, b, y, msg in messages:
        ya = y
        arrow(d, [(xs[a], ya), (xs[b], ya)], 4, 20)
        top = ya - 56
        # Label is above the arrow and has a white backplate, so dashed lifelines
        # never visually cut through the message text after Word scaling.
        label_xy = (min(xs[a], xs[b]) + 10, top, max(xs[a], xs[b]) - 10, top + 44)
        d.rectangle(label_xy, fill="white")
        center_text(d, label_xy, msg, 40, False)
    save_crop(img, filename)


def diagram_login_sequence() -> None:
    sequence_diagram(
        "image10_login_sequence.png",
        ["用户", "Web 登录页", "AuthController", "UserService", "数据库"],
        [
            (0, 1, 225, "输入账号密码"),
            (1, 2, 315, "POST /auth/login"),
            (2, 3, 405, "校验参数"),
            (3, 4, 495, "查询用户角色"),
            (4, 3, 585, "返回用户数据"),
            (3, 2, 675, "生成 JWT"),
            (2, 1, 765, "返回 Token"),
            (1, 0, 855, "进入首页"),
        ],
        1850,
        940,
    )


def diagram_realtime_flow() -> None:
    img, d = canvas(2000, 710)
    top = [(70, 90, "监测设备"), (395, 90, "数据接收"), (720, 90, "监测数据表"), (1045, 90, "阈值判断"), (1370, 90, "告警状态")]
    bottom = [(1370, 440, "分页表格"), (1045, 440, "趋势图表"), (720, 440, "实时监测页"), (380, 440, "WebSocket")]
    for x, y, t in top + bottom:
        rect(d, (x, y, x + 250, y + 82), t, 42)
    for a, b in zip(top, top[1:]):
        arrow(d, [(a[0] + 250, 131), (b[0], 131)], 5)
    arrow(d, [(1495, 172), (1495, 440)], 5)
    for a, b in zip(bottom, bottom[1:]):
        arrow(d, [(a[0], 481), (b[0] + 250, 481)], 5)
    arrow(d, [(505, 440), (505, 310), (1370, 310), (1370, 131)], 5)
    save_crop(img, "image11_realtime_flow.png")


def diagram_alert_generation() -> None:
    img, d = canvas(2000, 640)
    rect(d, (70, 270, 320, 360), "采集数据", 44)
    rect(d, (470, 270, 720, 360), "读取规则", 44)
    diamond(d, 970, 315, 250, 155, "是否超过\n阈值", 40)
    rect(d, (1250, 125, 1500, 215), "生成告警", 44)
    rect(d, (1640, 125, 1890, 215), "分级通知", 44)
    rect(d, (1250, 440, 1500, 530), "记录正常", 44)
    rect(d, (1640, 440, 1890, 530), "页面刷新", 44)
    arrow(d, [(320, 315), (470, 315)], 5)
    arrow(d, [(720, 315), (845, 315)], 5)
    arrow(d, [(1095, 315), (1165, 315), (1165, 170), (1250, 170)], 5)
    rect(d, (1135, 212, 1210, 268), "是", 34, False, 3, 4)
    arrow(d, [(1500, 170), (1640, 170)], 5)
    arrow(d, [(970, 392), (970, 485), (1250, 485)], 5)
    rect(d, (1010, 420, 1085, 476), "否", 34, False, 3, 4)
    arrow(d, [(1500, 485), (1640, 485)], 5)
    save_crop(img, "image12_alert_generation.png")


def diagram_ai_sequence() -> None:
    sequence_diagram(
        "image13_ai_sequence.png",
        ["用户", "AI 助手页", "后端服务", "数据库", "大模型接口"],
        [
            (0, 1, 215, "输入业务问题"),
            (1, 2, 310, "提交问题"),
            (2, 3, 405, "查询业务数据"),
            (3, 2, 500, "返回上下文"),
            (2, 4, 595, "调用模型接口"),
            (4, 2, 690, "返回分析结果"),
            (2, 1, 785, "返回格式化答复"),
        ],
        1850,
        880,
    )


def diagram_mini_order() -> None:
    img, d = canvas(2050, 690)
    top = [(70, 85, "工作台"), (395, 85, "待接收工单"), (720, 85, "接收处理"), (1045, 85, "查看详情"), (1370, 85, "现场处理")]
    lower = [(1370, 430, "提交反馈"), (1045, 430, "待验收"), (720, 430, "验收通过"), (395, 430, "已完成"), (70, 430, "退回处理")]
    for x, y, t in top + lower:
        rect(d, (x, y, x + 250, y + 82), t, 42)
    for a, b in zip(top, top[1:]):
        arrow(d, [(a[0] + 250, 126), (b[0], 126)], 5)
    arrow(d, [(1495, 167), (1495, 430)], 5)
    for a, b in zip(lower, lower[1:4]):
        arrow(d, [(a[0], 471), (b[0] + 250, 471)], 5)
    arrow(d, [(1045, 512), (1045, 595), (195, 595), (195, 512)], 5)
    arrow(d, [(320, 471), (395, 471)], 5)
    save_crop(img, "image14_mini_order.png")


def contact_sheet() -> None:
    names = [
        "image1_roles_permissions.png",
        "image2_business_flow.png",
        "image3_alert_loop.png",
        "image4_use_case.png",
        "image5_architecture.png",
        "image6_deployment.png",
        "image7_modules.png",
        "image8_er.png",
        "image9_class.png",
        "image10_login_sequence.png",
        "image11_realtime_flow.png",
        "image12_alert_generation.png",
        "image13_ai_sequence.png",
        "image14_mini_order.png",
    ]
    thumbs = []
    for name in names:
        im = Image.open(OUT / name).convert("RGB")
        ratio = im.height / im.width
        tw = 900
        th = max(1, int(tw * ratio))
        thumbs.append((name, im.resize((tw, th), Image.Resampling.LANCZOS)))
    pad = 60
    label_h = 54
    cols = 2
    row_h = max(im.height for _, im in thumbs) + label_h + pad
    sheet = Image.new("RGB", (cols * 900 + (cols + 1) * pad, math.ceil(len(thumbs) / cols) * row_h + pad), "white")
    draw = ImageDraw.Draw(sheet)
    name_font = f(34)
    for i, (name, im) in enumerate(thumbs):
        col = i % cols
        row = i // cols
        x = pad + col * (900 + pad)
        y = pad + row * row_h
        sheet.paste(im, (x, y))
        draw.text((x, y + im.height + 15), name, fill="black", font=name_font)
    sheet.save(OUT / "engineering_diagrams_stable_contact.png", quality=95, dpi=(220, 220))


def main() -> None:
    diagram_roles_permissions()
    diagram_business_flow()
    diagram_alert_loop()
    diagram_use_case()
    diagram_architecture()
    diagram_deployment()
    diagram_modules()
    diagram_er()
    diagram_class()
    diagram_login_sequence()
    diagram_realtime_flow()
    diagram_alert_generation()
    diagram_ai_sequence()
    diagram_mini_order()
    contact_sheet()


if __name__ == "__main__":
    main()
