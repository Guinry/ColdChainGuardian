from __future__ import annotations

import math
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


OUT = Path(r"C:\Users\23869\Desktop\ColdChainGuardian\output\doc\qa_v11\diagrams")
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


def text_size(d: ImageDraw.ImageDraw, text: str, font: ImageFont.FreeTypeFont) -> tuple[int, int]:
    box = d.textbbox((0, 0), text, font=font)
    return box[2] - box[0], box[3] - box[1]


def center_text(
    d: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    text: str,
    font: ImageFont.FreeTypeFont,
    gap: int = 8,
) -> None:
    x1, y1, x2, y2 = xy
    lines = text.split("\n")
    heights = [text_size(d, line, font)[1] for line in lines]
    total = sum(heights) + gap * (len(lines) - 1)
    y = y1 + (y2 - y1 - total) / 2
    for line, h in zip(lines, heights):
        w, _ = text_size(d, line, font)
        d.text((x1 + (x2 - x1 - w) / 2, y), line, font=font, fill="black")
        y += h + gap


def left_text(
    d: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    text: str,
    font: ImageFont.FreeTypeFont,
    gap: int = 7,
    pad: int = 22,
) -> None:
    x1, y1, _x2, _y2 = xy
    y = y1 + pad
    for line in text.split("\n"):
        d.text((x1 + pad, y), line, font=font, fill="black")
        y += text_size(d, line, font)[1] + gap


def rect(
    d: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    label: str,
    font: ImageFont.FreeTypeFont,
    width: int = 5,
    radius: int = 18,
) -> None:
    d.rounded_rectangle(xy, radius=radius, outline="black", width=width, fill="white")
    center_text(d, xy, label, font)


def plain_rect(
    d: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    width: int = 5,
) -> None:
    d.rectangle(xy, outline="black", width=width, fill="white")


def ellipse(
    d: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    label: str,
    font: ImageFont.FreeTypeFont,
    width: int = 5,
) -> None:
    d.ellipse(xy, outline="black", width=width, fill="white")
    center_text(d, xy, label, font)


def diamond(
    d: ImageDraw.ImageDraw,
    cx: int,
    cy: int,
    w: int,
    h: int,
    label: str,
    font: ImageFont.FreeTypeFont,
    width: int = 5,
) -> tuple[int, int, int, int]:
    points = [(cx, cy - h // 2), (cx + w // 2, cy), (cx, cy + h // 2), (cx - w // 2, cy)]
    d.polygon(points, outline="black", fill="white")
    d.line(points + [points[0]], fill="black", width=width)
    center_text(d, (cx - w // 2 + 20, cy - h // 2 + 18, cx + w // 2 - 20, cy + h // 2 - 18), label, font, gap=4)
    return (cx - w // 2, cy - h // 2, cx + w // 2, cy + h // 2)


def line(d: ImageDraw.ImageDraw, pts: list[tuple[int, int]], width: int = 5) -> None:
    d.line(pts, fill="black", width=width, joint="curve")


def arrow_head(
    d: ImageDraw.ImageDraw,
    p1: tuple[int, int],
    p2: tuple[int, int],
    size: int = 24,
) -> None:
    angle = math.atan2(p2[1] - p1[1], p2[0] - p1[0])
    left = (p2[0] - size * math.cos(angle - math.pi / 6), p2[1] - size * math.sin(angle - math.pi / 6))
    right = (p2[0] - size * math.cos(angle + math.pi / 6), p2[1] - size * math.sin(angle + math.pi / 6))
    d.polygon([p2, left, right], fill="black")


def arrow(d: ImageDraw.ImageDraw, pts: list[tuple[int, int]], width: int = 5, head: int = 24) -> None:
    line(d, pts, width)
    arrow_head(d, pts[-2], pts[-1], head)


def dashed_rect(d: ImageDraw.ImageDraw, xy: tuple[int, int, int, int], width: int = 5, dash: int = 26) -> None:
    x1, y1, x2, y2 = xy
    for a, b in [((x1, y1), (x2, y1)), ((x2, y1), (x2, y2)), ((x2, y2), (x1, y2)), ((x1, y2), (x1, y1))]:
        dashed_line(d, a, b, width, dash)


def dashed_line(
    d: ImageDraw.ImageDraw,
    p1: tuple[int, int],
    p2: tuple[int, int],
    width: int = 5,
    dash: int = 26,
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
        pos += dash


def actor(d: ImageDraw.ImageDraw, cx: int, cy: int, label: str, font: ImageFont.FreeTypeFont) -> None:
    d.ellipse((cx - 28, cy - 104, cx + 28, cy - 48), outline="black", width=5)
    d.line([(cx, cy - 48), (cx, cy + 44)], fill="black", width=5)
    d.line([(cx - 62, cy - 2), (cx + 62, cy - 2)], fill="black", width=5)
    d.line([(cx, cy + 44), (cx - 56, cy + 120)], fill="black", width=5)
    d.line([(cx, cy + 44), (cx + 56, cy + 120)], fill="black", width=5)
    center_text(d, (cx - 150, cy + 128, cx + 150, cy + 195), label, font)


def class_box(
    d: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    name: str,
    attrs: list[str],
    methods: list[str],
    name_font: ImageFont.FreeTypeFont,
    body_font: ImageFont.FreeTypeFont,
) -> None:
    x1, y1, x2, y2 = xy
    plain_rect(d, xy, width=5)
    d.line([(x1, y1 + 82), (x2, y1 + 82)], fill="black", width=4)
    d.line([(x1, y2 - 82), (x2, y2 - 82)], fill="black", width=4)
    center_text(d, (x1, y1, x2, y1 + 82), name, name_font)
    left_text(d, (x1, y1 + 82, x2, y2 - 82), "\n".join(attrs), body_font, gap=2, pad=18)
    left_text(d, (x1, y2 - 82, x2, y2), "\n".join(methods), body_font, gap=2, pad=18)


def save(img: Image.Image, name: str) -> None:
    img.save(OUT / name, quality=95, dpi=(300, 300))


def diagram_roles_permissions() -> None:
    img, d = canvas(2200, 1180)
    title = f(66, True)
    body = f(58)
    small = f(48)
    roles = [
        ((120, 150, 500, 280), "系统管理员", ["用户与角色", "系统配置", "全局数据"]),
        ((120, 390, 500, 520), "仓储管理员", ["库区设备", "监测告警", "工单管理"]),
        ((120, 630, 500, 760), "现场员工", ["移动工单", "处置反馈", "个人信息"]),
        ((120, 870, 500, 1000), "大模型服务", ["数据查询", "风险分析", "辅助建议"]),
    ]
    for role_box, role_name, scopes in roles:
        rect(d, role_box, role_name, title)
        x1, y1, x2, y2 = 760, role_box[1] - 20, 2020, role_box[3] + 20
        plain_rect(d, (x1, y1, x2, y2), width=4)
        step = (x2 - x1 - 80) // len(scopes)
        for i, scope in enumerate(scopes):
            sx1 = x1 + 40 + i * step
            sx2 = sx1 + step - 30
            rect(d, (sx1, y1 + 35, sx2, y2 - 35), scope, body, width=4, radius=14)
            arrow(d, [(role_box[2], (role_box[1] + role_box[3]) // 2), (sx1, (role_box[1] + role_box[3]) // 2)], width=4, head=22)
    center_text(d, (760, 1040, 2020, 1115), "角色权限以最小授权为原则，覆盖后台管理端、小程序端和智能分析服务", small)
    save(img, "image1_roles_permissions.png")


def diagram_business_flow() -> None:
    img, d = canvas(2300, 1280)
    body = f(60)
    note = f(50)
    top = [
        (120, 160, 440, 290, "设备采集"),
        (570, 160, 890, 290, "数据接入"),
        (1020, 160, 1340, 290, "阈值判断"),
        (1470, 160, 1790, 290, "告警生成"),
        (1920, 160, 2240, 290, "实时推送"),
    ]
    mid = [
        (350, 520, 670, 650, "告警研判"),
        (820, 520, 1140, 650, "工单派发"),
        (1290, 520, 1610, 650, "现场处理"),
        (1760, 520, 2080, 650, "验收关闭"),
    ]
    bottom = [
        (350, 890, 670, 1020, "闭环记录"),
        (820, 890, 1140, 1020, "趋势统计"),
        (1290, 890, 1610, 1020, "风险分析"),
        (1760, 890, 2080, 1020, "复盘优化"),
    ]
    for row in [top, mid, bottom]:
        for x1, y1, x2, y2, label in row:
            rect(d, (x1, y1, x2, y2), label, body)
        for a, b in zip(row, row[1:]):
            arrow(d, [(a[2], (a[1] + a[3]) // 2), (b[0], (b[1] + b[3]) // 2)], width=5)
    arrow(d, [(2080, 290), (2080, 400), (510, 400), (510, 520)], width=5)
    for a, b in zip(mid, bottom):
        arrow(d, [((a[0] + a[2]) // 2, a[3]), ((b[0] + b[2]) // 2, b[1])], width=5)
    center_text(d, (180, 1090, 2120, 1180), "业务主线围绕监测数据、告警研判、工单处置和统计复盘形成安全管理闭环", note)
    save(img, "image2_business_flow.png")


def diagram_alert_loop() -> None:
    img, d = canvas(2300, 1120)
    body = f(60)
    small = f(46)
    nodes = {
        "alert": (120, 210, 420, 340, "告警生成"),
        "review": (560, 210, 860, 340, "管理研判"),
        "order": (1000, 210, 1300, 340, "转为工单"),
        "accept": (1440, 210, 1740, 340, "员工接收"),
        "handle": (1880, 210, 2180, 340, "现场处理"),
        "ignore": (560, 640, 860, 770, "误报关闭"),
        "solve": (1000, 640, 1300, 770, "直接解决"),
        "check": (1880, 640, 2180, 770, "管理验收"),
        "close": (1440, 640, 1740, 770, "闭环归档"),
    }
    for x1, y1, x2, y2, label in nodes.values():
        rect(d, (x1, y1, x2, y2), label, body)
    for a, b in [("alert", "review"), ("review", "order"), ("order", "accept"), ("accept", "handle")]:
        ax = nodes[a][2]
        ay = (nodes[a][1] + nodes[a][3]) // 2
        bx = nodes[b][0]
        by = (nodes[b][1] + nodes[b][3]) // 2
        arrow(d, [(ax, ay), (bx, by)], width=5)
    arrow(d, [(710, 340), (710, 640)], width=5)
    arrow(d, [(1150, 340), (1150, 640)], width=5)
    arrow(d, [(2030, 340), (2030, 640)], width=5)
    arrow(d, [(1880, 705), (1740, 705)], width=5)
    arrow(d, [(1440, 705), (1300, 705)], width=5)
    arrow(d, [(1150, 770), (1150, 920), (2030, 920), (2030, 770)], width=5)
    center_text(d, (160, 930, 2140, 1020), "异常确认后进入工单闭环；误报、直接解决和验收不通过均保留可追溯记录", small)
    save(img, "image3_alert_loop.png")


def diagram_use_case() -> None:
    img, d = canvas(2400, 1320)
    actor_font = f(52)
    use_font = f(54)
    boundary_font = f(48)
    dashed_rect(d, (430, 80, 1970, 1230), width=5, dash=32)
    center_text(d, (460, 95, 760, 155), "系统边界", boundary_font)
    cases = {
        "login": (610, 180, 1010, 290, "登录认证"),
        "system": (610, 360, 1010, 470, "系统管理"),
        "area": (610, 540, 1010, 650, "库区设备管理"),
        "monitor": (610, 720, 1010, 830, "实时监测"),
        "rule": (610, 900, 1010, 1010, "阈值规则"),
        "alert": (1350, 270, 1750, 380, "告警处理"),
        "order": (1350, 500, 1750, 610, "工单闭环"),
        "analysis": (1350, 730, 1750, 840, "数据分析"),
        "ai": (1350, 960, 1750, 1070, "AI 智能问答"),
    }
    for xy in cases.values():
        ellipse(d, xy[:4], xy[4], use_font)
    actor(d, 190, 300, "系统管理员", actor_font)
    actor(d, 190, 820, "仓储管理员", actor_font)
    actor(d, 2210, 520, "现场员工", actor_font)
    actor(d, 2210, 980, "大模型服务", actor_font)
    arrow(d, [(280, 300), (430, 300), (430, 235), (610, 235)], width=4, head=0)
    arrow(d, [(280, 300), (430, 300), (430, 415), (610, 415)], width=4, head=0)
    arrow(d, [(280, 820), (430, 820), (430, 595), (610, 595)], width=4, head=0)
    arrow(d, [(280, 820), (430, 820), (430, 775), (610, 775)], width=4, head=0)
    arrow(d, [(280, 820), (430, 820), (430, 955), (610, 955)], width=4, head=0)
    arrow(d, [(1970, 520), (2040, 520), (2040, 555), (1750, 555)], width=4, head=0)
    arrow(d, [(1970, 520), (2040, 520), (2040, 325), (1750, 325)], width=4, head=0)
    arrow(d, [(1970, 980), (2040, 980), (2040, 1015), (1750, 1015)], width=4, head=0)
    arrow(d, [(1010, 595), (1180, 595), (1180, 775), (1350, 775)], width=4)
    arrow(d, [(1010, 775), (1180, 775), (1180, 325), (1350, 325)], width=4)
    arrow(d, [(1750, 325), (1870, 325), (1870, 555), (1750, 555)], width=4)
    save(img, "image4_use_case.png")


def diagram_architecture() -> None:
    img, d = canvas(2300, 1180)
    layer_font = f(58, True)
    node_font = f(52)
    band_font = f(46)
    bands = [
        (90, 110, 2210, 290, "表现层", ["Web 管理端", "微信小程序端", "PC 浏览器"]),
        (90, 370, 2210, 550, "接入层", ["REST API", "JWT 认证", "WebSocket 推送"]),
        (90, 630, 2210, 810, "业务层", ["库区管理", "设备管理", "实时监测", "阈值规则", "告警中心", "工单闭环", "数据分析", "AI 助手"]),
        (90, 890, 2210, 1070, "数据与外部服务层", ["MySQL", "Redis", "日志审计", "大模型接口"]),
    ]
    for x1, y1, x2, y2, title, nodes in bands:
        plain_rect(d, (x1, y1, x2, y2), width=5)
        center_text(d, (x1, y1, x1 + 260, y2), title, layer_font)
        nx = x1 + 330
        gap = 28
        nw = (x2 - nx - 40 - gap * (len(nodes) - 1)) // len(nodes)
        for i, node in enumerate(nodes):
            bx1 = nx + i * (nw + gap)
            rect(d, (bx1, y1 + 48, bx1 + nw, y2 - 48), node, node_font, width=4, radius=14)
    for x in [760, 1160, 1560]:
        arrow(d, [(x, 290), (x, 370)], width=5)
        arrow(d, [(x, 550), (x, 630)], width=5)
        arrow(d, [(x, 810), (x, 890)], width=5)
    center_text(d, (180, 1092, 2120, 1150), "各层通过接口契约解耦，支撑后台页面、小程序作业、实时推送和智能分析协同运行", band_font)
    save(img, "image5_architecture.png")


def diagram_deployment() -> None:
    img, d = canvas(2300, 1180)
    title = f(58, True)
    body = f(52)
    small = f(44)
    groups = [
        (90, 120, 520, 1000, "客户端"),
        (650, 120, 1180, 1000, "应用服务"),
        (1310, 120, 1740, 1000, "数据服务"),
        (1870, 120, 2210, 1000, "外部服务"),
    ]
    for x1, y1, x2, y2, label in groups:
        dashed_rect(d, (x1, y1, x2, y2), width=5, dash=30)
        center_text(d, (x1, y1 + 20, x2, y1 + 90), label, title)
    clients = [(160, 260, 450, 390, "Web\n管理端"), (160, 550, 450, 680, "微信\n小程序端")]
    apps = [(735, 250, 1095, 400, "Spring Boot\n后端 8080"), (735, 520, 1095, 650, "WebSocket\n服务"), (735, 770, 1095, 900, "AI 上下文\n构造")]
    data = [(1370, 250, 1680, 380, "MySQL\n数据库"), (1370, 500, 1680, 630, "Redis\n缓存"), (1370, 750, 1680, 880, "文件与日志")]
    external = [(1910, 440, 2170, 610, "兼容式\n大模型接口\n deepseek-v4-pro")]
    for item in clients + apps + data + external:
        rect(d, item[:4], item[4], body, width=5, radius=16)
    arrow(d, [(450, 325), (735, 325)], width=5)
    center_text(d, (500, 260, 700, 315), "HTTPS\nREST API", small)
    arrow(d, [(450, 615), (735, 585)], width=5)
    center_text(d, (500, 555, 700, 640), "HTTPS\nREST API", small)
    arrow(d, [(1095, 325), (1370, 315)], width=5)
    arrow(d, [(1095, 585), (1370, 565)], width=5)
    arrow(d, [(1095, 835), (1370, 815)], width=5)
    arrow(d, [(1095, 835), (1910, 525)], width=5)
    save(img, "image6_deployment.png")


def diagram_modules() -> None:
    img, d = canvas(2400, 1220)
    root_font = f(62, True)
    body = f(50)
    root = (820, 90, 1580, 200, "冷链仓储安全管理系统")
    rect(d, root[:4], root[4], root_font)
    groups = [
        (120, 380, 650, 500, "Web 管理端", ["Dashboard", "库区管理", "设备管理", "实时监测", "阈值规则", "告警中心", "工单中心", "数据分析", "系统管理", "AI 助手"]),
        (710, 380, 1240, 500, "微信小程序端", ["登录认证", "工作台", "告警查看", "工单处理", "反馈提交", "个人中心"]),
        (1300, 380, 1830, 500, "后端服务", ["认证授权", "业务接口", "实时推送", "数据持久化", "日志审计"]),
        (1890, 380, 2280, 500, "智能分析", ["上下文构造", "数据摘要", "风险解释", "处置建议"]),
    ]
    for gx1, gy1, gx2, gy2, label, children in groups:
        rect(d, (gx1, gy1, gx2, gy2), label, root_font)
        arrow(d, [((root[0] + root[2]) // 2, root[3]), ((root[0] + root[2]) // 2, 285), ((gx1 + gx2) // 2, 285), ((gx1 + gx2) // 2, gy1)], width=5)
        cols = 2 if len(children) > 5 else 1
        cw = (gx2 - gx1 - 60) // cols
        for i, child in enumerate(children):
            col = i % cols
            row = i // cols
            x1 = gx1 + 25 + col * cw
            y1 = 560 + row * 95
            rect(d, (x1, y1, x1 + cw - 25, y1 + 62), child, body, width=4, radius=12)
    save(img, "image7_modules.png")


def diagram_er() -> None:
    img, d = canvas(2400, 1420)
    name_font = f(54, True)
    field_font = f(44)
    entities = {
        "user": (120, 120, 470, 330, "用户", ["user_id", "username", "role_id"]),
        "role": (660, 120, 1010, 330, "角色", ["role_id", "role_name", "permission_set"]),
        "area": (120, 560, 470, 770, "库区", ["area_id", "parent_id", "area_name"]),
        "device": (660, 560, 1010, 770, "设备", ["device_id", "area_id", "device_status"]),
        "data": (1200, 560, 1550, 770, "监测数据", ["data_id", "device_id", "temperature"]),
        "rule": (1200, 120, 1550, 330, "阈值规则", ["rule_id", "area_id", "min_value/max_value"]),
        "alert": (1740, 560, 2090, 770, "告警", ["alert_id", "device_id", "alert_status"]),
        "order": (1740, 960, 2090, 1170, "工单", ["order_id", "alert_id", "assignee_id"]),
        "ai": (660, 960, 1010, 1170, "AI 会话", ["session_id", "user_id", "created_at"]),
        "msg": (1200, 960, 1550, 1170, "AI 消息", ["message_id", "session_id", "role/content"]),
    }
    for x1, y1, x2, y2, name, fields in entities.values():
        plain_rect(d, (x1, y1, x2, y2), width=5)
        d.line([(x1, y1 + 75), (x2, y1 + 75)], fill="black", width=4)
        center_text(d, (x1, y1, x2, y1 + 75), name, name_font)
        left_text(d, (x1, y1 + 75, x2, y2), "\n".join(fields), field_font, gap=4, pad=22)
    links = [
        ("user", "role", "N:1"),
        ("area", "device", "1:N"),
        ("device", "data", "1:N"),
        ("rule", "alert", "匹配触发"),
        ("device", "alert", "1:N"),
        ("alert", "order", "1:1"),
        ("user", "ai", "1:N"),
        ("ai", "msg", "1:N"),
    ]
    centers = {k: ((v[0] + v[2]) // 2, (v[1] + v[3]) // 2, v) for k, v in entities.items()}
    for a, b, label in links:
        ax, ay, av = centers[a]
        bx, by, bv = centers[b]
        if abs(ax - bx) > abs(ay - by):
            p1 = (av[2], ay) if ax < bx else (av[0], ay)
            p2 = (bv[0], by) if ax < bx else (bv[2], by)
            mid = ((p1[0] + p2[0]) // 2, p1[1])
            pts = [p1, mid, (mid[0], p2[1]), p2]
        else:
            p1 = (ax, av[3]) if ay < by else (ax, av[1])
            p2 = (bx, bv[1]) if ay < by else (bx, bv[3])
            mid = (p1[0], (p1[1] + p2[1]) // 2)
            pts = [p1, mid, (p2[0], mid[1]), p2]
        line(d, pts, width=4)
        center_text(d, (min(p[0] for p in pts) + 8, min(p[1] for p in pts) + 8, max(p[0] for p in pts) - 8, max(p[1] for p in pts) + 60), label, field_font, gap=2)
    save(img, "image8_er.png")


def diagram_class() -> None:
    img, d = canvas(2400, 1400)
    name = f(48, True)
    body = f(39)
    boxes = {
        "user": (120, 100, 470, 360, "User", ["id", "username", "roleId"], ["login()", "assignRole()"]),
        "area": (610, 100, 960, 360, "Area", ["id", "parentId", "name"], ["buildTree()", "update()"]),
        "device": (1100, 100, 1450, 360, "Device", ["id", "areaId", "status"], ["bindArea()", "enable()"]),
        "data": (1590, 100, 1940, 360, "SensorData", ["id", "deviceId", "temp"], ["save()", "latest()"]),
        "rule": (610, 520, 960, 780, "ThresholdRule", ["id", "targetId", "min/max"], ["match()", "enable()"]),
        "alert": (1100, 520, 1450, 780, "Alert", ["id", "level", "status"], ["confirm()", "close()"]),
        "order": (1590, 520, 1940, 780, "WorkOrder", ["id", "alertId", "assignee"], ["dispatch()", "accept()"]),
        "session": (610, 940, 960, 1200, "AiSession", ["id", "userId", "title"], ["create()", "summarize()"]),
        "message": (1100, 940, 1450, 1200, "AiMessage", ["id", "sessionId", "role"], ["append()", "render()"]),
    }
    for x1, y1, x2, y2, cls, attrs, methods in boxes.values():
        class_box(d, (x1, y1, x2, y2), cls, attrs, methods, name, body)
    def edge(a: str, b: str) -> None:
        av = boxes[a]
        bv = boxes[b]
        ax, ay = (av[0] + av[2]) // 2, (av[1] + av[3]) // 2
        bx, by = (bv[0] + bv[2]) // 2, (bv[1] + bv[3]) // 2
        if abs(ax - bx) >= abs(ay - by):
            p1 = (av[2], ay) if ax < bx else (av[0], ay)
            p2 = (bv[0], by) if ax < bx else (bv[2], by)
            line(d, [p1, ((p1[0] + p2[0]) // 2, p1[1]), ((p1[0] + p2[0]) // 2, p2[1]), p2], width=4)
        else:
            p1 = (ax, av[3]) if ay < by else (ax, av[1])
            p2 = (bx, bv[1]) if ay < by else (bx, bv[3])
            line(d, [p1, (p1[0], (p1[1] + p2[1]) // 2), (p2[0], (p1[1] + p2[1]) // 2), p2], width=4)
    for a, b in [("area", "device"), ("device", "data"), ("rule", "alert"), ("device", "alert"), ("alert", "order"), ("user", "session"), ("session", "message")]:
        edge(a, b)
    save(img, "image9_class.png")


def diagram_login_sequence() -> None:
    img, d = canvas(2300, 1120)
    top_font = f(52)
    msg_font = f(44)
    xs = [210, 620, 1030, 1440, 1850]
    labels = ["用户", "Web 登录页", "AuthController", "UserService", "数据库"]
    for x, label in zip(xs, labels):
        rect(d, (x - 150, 80, x + 150, 170), label, top_font, width=4)
        dashed_line(d, (x, 170), (x, 980), width=3, dash=24)
    messages = [
        (0, 1, 260, "输入账号密码"),
        (1, 2, 370, "POST /auth/login"),
        (2, 3, 480, "校验登录参数"),
        (3, 4, 590, "查询用户与角色"),
        (4, 3, 700, "返回用户数据"),
        (3, 2, 810, "生成 JWT"),
        (2, 1, 900, "返回 Token"),
        (1, 0, 980, "进入 Dashboard"),
    ]
    for a, b, y, msg in messages:
        arrow(d, [(xs[a], y), (xs[b], y)], width=4, head=22)
        center_text(d, (min(xs[a], xs[b]) + 10, y - 58, max(xs[a], xs[b]) - 10, y - 12), msg, msg_font)
    save(img, "image10_login_sequence.png")


def diagram_realtime_flow() -> None:
    img, d = canvas(2300, 980)
    body = f(60)
    items = [
        (120, 150, 420, 280, "监测设备"),
        (560, 150, 860, 280, "数据接收"),
        (1000, 150, 1300, 280, "监测数据表"),
        (1440, 150, 1740, 280, "阈值判断"),
        (1880, 150, 2180, 280, "告警状态"),
        (1880, 580, 2180, 710, "分页表格"),
        (1440, 580, 1740, 710, "趋势图表"),
        (1000, 580, 1300, 710, "实时监测页"),
        (560, 580, 860, 710, "WebSocket"),
    ]
    for x1, y1, x2, y2, label in items:
        rect(d, (x1, y1, x2, y2), label, body)
    for a, b in zip(items[:5], items[1:5]):
        arrow(d, [(a[2], 215), (b[0], 215)], width=5)
    arrow(d, [(2030, 280), (2030, 580)], width=5)
    for a, b in [(items[5], items[6]), (items[6], items[7]), (items[7], items[8])]:
        arrow(d, [(a[0], 645), (b[2], 645)], width=5)
    arrow(d, [(710, 580), (710, 430), (1880, 430), (1880, 215)], width=5)
    save(img, "image11_realtime_flow.png")


def diagram_alert_generation() -> None:
    img, d = canvas(2300, 980)
    body = f(58)
    small = f(44)
    rect(d, (120, 380, 420, 510), "采集数据", body)
    rect(d, (560, 380, 860, 510), "读取规则", body)
    diamond(d, 1140, 445, 310, 190, "是否超过\n阈值", body)
    rect(d, (1430, 220, 1730, 350), "生成告警", body)
    rect(d, (1870, 220, 2170, 350), "分级通知", body)
    rect(d, (1430, 620, 1730, 750), "记录正常", body)
    rect(d, (1870, 620, 2170, 750), "页面刷新", body)
    arrow(d, [(420, 445), (560, 445)], width=5)
    arrow(d, [(860, 445), (985, 445)], width=5)
    arrow(d, [(1295, 445), (1360, 445), (1360, 285), (1430, 285)], width=5)
    center_text(d, (1280, 315, 1425, 390), "是", small)
    arrow(d, [(1730, 285), (1870, 285)], width=5)
    arrow(d, [(1140, 540), (1140, 685), (1430, 685)], width=5)
    center_text(d, (1160, 565, 1320, 640), "否", small)
    arrow(d, [(1730, 685), (1870, 685)], width=5)
    save(img, "image12_alert_generation.png")


def diagram_ai_sequence() -> None:
    img, d = canvas(2400, 1120)
    top_font = f(52)
    msg_font = f(42)
    xs = [160, 520, 880, 1240, 1600, 1960, 2260]
    labels = ["用户", "AI 助手页", "后端接口", "上下文服务", "数据库", "大模型接口", "消息记录"]
    for x, label in zip(xs, labels):
        rect(d, (x - 130, 80, x + 130, 170), label, top_font, width=4)
        dashed_line(d, (x, 170), (x, 990), width=3, dash=24)
    messages = [
        (0, 1, 250, "输入业务问题"),
        (1, 2, 345, "提交问题"),
        (2, 3, 440, "构造分析任务"),
        (3, 4, 535, "查询监测与告警数据"),
        (4, 3, 630, "返回业务上下文"),
        (3, 5, 725, "调用兼容式接口"),
        (5, 3, 820, "返回分析结果"),
        (3, 6, 915, "保存会话消息"),
        (2, 1, 1000, "返回 Markdown 答复"),
    ]
    for a, b, y, msg in messages:
        arrow(d, [(xs[a], y), (xs[b], y)], width=4, head=22)
        center_text(d, (min(xs[a], xs[b]) + 10, y - 54, max(xs[a], xs[b]) - 10, y - 10), msg, msg_font)
    save(img, "image13_ai_sequence.png")


def diagram_mini_order() -> None:
    img, d = canvas(2300, 980)
    body = f(58)
    small = f(44)
    rect(d, (100, 150, 390, 280), "工作台", body)
    rect(d, (520, 150, 810, 280), "待接收工单", body)
    rect(d, (940, 150, 1230, 280), "接收处理", body)
    rect(d, (1360, 150, 1650, 280), "查看详情", body)
    rect(d, (1780, 150, 2070, 280), "现场处置", body)
    rect(d, (1780, 600, 2070, 730), "提交反馈", body)
    rect(d, (1360, 600, 1650, 730), "待验收", body)
    diamond(d, 1085, 665, 270, 180, "验收\n是否通过", body)
    rect(d, (720, 600, 1010, 730), "已完成", body)
    rect(d, (300, 600, 590, 730), "退回处理", body)
    top = [(100, 150, 390, 280), (520, 150, 810, 280), (940, 150, 1230, 280), (1360, 150, 1650, 280), (1780, 150, 2070, 280)]
    for a, b in zip(top, top[1:]):
        arrow(d, [(a[2], 215), (b[0], 215)], width=5)
    arrow(d, [(1925, 280), (1925, 600)], width=5)
    arrow(d, [(1780, 665), (1650, 665)], width=5)
    arrow(d, [(1360, 665), (1220, 665)], width=5)
    arrow(d, [(950, 665), (1010, 665)], width=5)
    arrow(d, [(950, 665), (590, 665)], width=5)
    center_text(d, (950, 570, 1040, 625), "通过", small)
    center_text(d, (690, 570, 830, 625), "不通过", small)
    save(img, "image14_mini_order.png")


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
    thumb_w = 950
    pad = 70
    label_h = 55
    cols = 2
    rows = math.ceil(len(names) / cols)
    thumbs: list[tuple[str, Image.Image]] = []
    for name in names:
        im = Image.open(OUT / name)
        ratio = im.height / im.width
        thumbs.append((name, im.resize((thumb_w, int(thumb_w * ratio)))))
    row_h = max(im.height for _, im in thumbs) + label_h + pad
    sheet = Image.new("RGB", (cols * thumb_w + (cols + 1) * pad, rows * row_h + pad), "white")
    draw = ImageDraw.Draw(sheet)
    label_font = f(36)
    for i, (name, im) in enumerate(thumbs):
        col = i % cols
        row = i // cols
        x = pad + col * (thumb_w + pad)
        y = pad + row * row_h
        sheet.paste(im, (x, y))
        draw.text((x, y + im.height + 15), name, fill="black", font=label_font)
    sheet.save(OUT / "engineering_diagrams_clean_contact.png", quality=95, dpi=(220, 220))


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
