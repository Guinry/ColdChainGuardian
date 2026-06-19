from __future__ import annotations

import importlib.util
from pathlib import Path


BASE = Path(r"C:\Users\23869\Desktop\ColdChainGuardian\tools\generate_v11_clean_engineering_diagrams.py")
spec = importlib.util.spec_from_file_location("clean_diagrams", BASE)
clean = importlib.util.module_from_spec(spec)
assert spec.loader is not None
spec.loader.exec_module(clean)


def diagram_architecture() -> None:
    img, d = clean.canvas(2200, 980)
    layer_font = clean.f(58, True)
    node_font = clean.f(50)
    note_font = clean.f(42)
    bands = [
        (90, 80, 2110, 240, "表现层", ["Web 管理端", "微信小程序端", "PC 浏览器"]),
        (90, 300, 2110, 460, "接入层", ["REST API", "JWT 认证", "WebSocket 推送"]),
        (90, 520, 2110, 680, "业务层", ["库区设备", "实时监测", "告警工单", "数据分析", "AI 助手"]),
        (90, 740, 2110, 900, "数据层", ["MySQL", "Redis", "日志审计", "大模型接口"]),
    ]
    for x1, y1, x2, y2, title, nodes in bands:
        clean.plain_rect(d, (x1, y1, x2, y2), width=5)
        clean.center_text(d, (x1, y1, x1 + 250, y2), title, layer_font)
        nx = x1 + 320
        gap = 32
        nw = (x2 - nx - 45 - gap * (len(nodes) - 1)) // len(nodes)
        for i, node in enumerate(nodes):
            bx1 = nx + i * (nw + gap)
            clean.rect(d, (bx1, y1 + 46, bx1 + nw, y2 - 46), node, node_font, width=4, radius=14)
    for x in [720, 1120, 1520]:
        clean.arrow(d, [(x, 240), (x, 300)], width=5)
        clean.arrow(d, [(x, 460), (x, 520)], width=5)
        clean.arrow(d, [(x, 680), (x, 740)], width=5)
    clean.center_text(d, (180, 910, 2020, 965), "分层结构通过接口契约解耦，支撑后台管理、小程序作业、实时推送和智能分析协同运行", note_font)
    clean.save(img, "image5_architecture.png")


def diagram_use_case() -> None:
    img, d = clean.canvas(2400, 1320)
    actor_font = clean.f(52)
    use_font = clean.f(54)
    boundary_font = clean.f(48)

    clean.dashed_rect(d, (430, 80, 1970, 1230), width=5, dash=32)
    clean.center_text(d, (470, 95, 760, 155), "系统边界", boundary_font)

    cases = {
        "login": (610, 185, 1010, 295, "登录认证"),
        "system": (610, 365, 1010, 475, "系统管理"),
        "area": (610, 545, 1010, 655, "库区设备管理"),
        "monitor": (610, 725, 1010, 835, "实时监测"),
        "rule": (610, 905, 1010, 1015, "阈值规则"),
        "alert": (1350, 265, 1750, 375, "告警处理"),
        "order": (1350, 505, 1750, 615, "工单闭环"),
        "analysis": (1350, 745, 1750, 855, "数据分析"),
        "ai": (1350, 985, 1750, 1095, "AI 智能问答"),
    }
    for xy in cases.values():
        clean.ellipse(d, xy[:4], xy[4], use_font)

    clean.actor(d, 185, 320, "系统管理员", actor_font)
    clean.actor(d, 185, 850, "仓储管理员", actor_font)
    clean.actor(d, 2215, 520, "现场员工", actor_font)
    clean.actor(d, 2215, 990, "大模型服务", actor_font)

    # UML associations use plain straight or orthogonal lines, not flow arrows.
    def assoc(points: list[tuple[int, int]]) -> None:
        clean.line(d, points, width=5)

    assoc([(270, 320), (430, 320), (430, 240), (610, 240)])
    assoc([(270, 320), (430, 320), (430, 420), (610, 420)])
    assoc([(270, 850), (430, 850), (430, 600), (610, 600)])
    assoc([(270, 850), (430, 850), (430, 780), (610, 780)])
    assoc([(270, 850), (430, 850), (430, 960), (610, 960)])
    assoc([(1750, 320), (1970, 320), (1970, 520), (2130, 520)])
    assoc([(1750, 560), (1970, 560), (1970, 520), (2130, 520)])
    assoc([(1750, 1040), (1970, 1040), (1970, 990), (2130, 990)])
    clean.arrow(d, [(1010, 600), (1180, 600), (1180, 800), (1350, 800)], width=5, head=24)
    clean.arrow(d, [(1010, 780), (1180, 780), (1180, 320), (1350, 320)], width=5, head=24)
    clean.arrow(d, [(1550, 375), (1550, 505)], width=5, head=24)
    clean.save(img, "image4_use_case.png")


def diagram_deployment() -> None:
    img, d = clean.canvas(2300, 1180)
    title = clean.f(58, True)
    body = clean.f(52)
    small = clean.f(44)
    groups = [
        (90, 120, 520, 1000, "客户端"),
        (650, 120, 1180, 1000, "应用服务"),
        (1310, 120, 1740, 1000, "数据服务"),
        (1870, 120, 2210, 1000, "外部服务"),
    ]
    for x1, y1, x2, y2, label in groups:
        clean.dashed_rect(d, (x1, y1, x2, y2), width=5, dash=30)
        clean.center_text(d, (x1, y1 + 20, x2, y1 + 90), label, title)

    clients = [(160, 260, 450, 390, "Web\n管理端"), (160, 550, 450, 680, "微信\n小程序端")]
    apps = [(735, 250, 1095, 400, "Spring Boot\n后端 8080"), (735, 520, 1095, 650, "WebSocket\n服务"), (735, 770, 1095, 900, "AI 上下文\n构造")]
    data = [(1370, 250, 1680, 380, "MySQL\n数据库"), (1370, 500, 1680, 630, "Redis\n缓存"), (1370, 750, 1680, 880, "文件与日志")]
    external = [(1890, 420, 2190, 650, "兼容式\n大模型接口\n deepseek-v4-pro")]
    for item in clients + apps + data + external:
        clean.rect(d, item[:4], item[4], body, width=5, radius=16)

    clean.arrow(d, [(450, 325), (735, 325)], width=5)
    clean.center_text(d, (500, 255, 700, 315), "HTTPS\nREST API", small)
    clean.arrow(d, [(450, 615), (735, 585)], width=5)
    clean.center_text(d, (500, 555, 700, 645), "HTTPS\nREST API", small)
    clean.arrow(d, [(1095, 325), (1370, 315)], width=5)
    clean.arrow(d, [(1095, 585), (1370, 565)], width=5)
    clean.arrow(d, [(1095, 835), (1370, 815)], width=5)
    clean.arrow(d, [(1095, 835), (1220, 835), (1220, 960), (1840, 960), (1840, 535), (1905, 535)], width=5)
    clean.save(img, "image6_deployment.png")


def diagram_modules() -> None:
    img, d = clean.canvas(2600, 1160)
    root_font = clean.f(64, True)
    group_font = clean.f(56, True)
    body = clean.f(50)
    list_font = clean.f(46)
    root = (880, 70, 1720, 180, "冷链仓储安全管理系统")
    clean.rect(d, root[:4], root[4], root_font)

    groups = [
        (100, 360, 680, 1040, "Web 管理端", ["Dashboard 与全局检索", "库区、设备维护", "实时监测与阈值规则", "告警、工单与数据分析", "系统管理与 AI 助手"]),
        (735, 360, 1315, 1040, "微信小程序端", ["登录认证与工作台", "告警查看与任务提醒", "工单接收与现场处置", "反馈提交", "个人信息与记录查询"]),
        (1370, 360, 1950, 1040, "后端服务", ["认证授权与角色权限", "业务接口与参数校验", "WebSocket 实时推送", "数据持久化", "日志审计"]),
        (2005, 360, 2485, 1040, "智能分析", ["上下文构造", "数据摘要", "风险解释", "处置建议"]),
    ]
    trunk_y = 270
    clean.line(d, [((root[0] + root[2]) // 2, root[3]), ((root[0] + root[2]) // 2, trunk_y)], width=5)
    clean.line(d, [(390, trunk_y), (2245, trunk_y)], width=5)
    for gx1, gy1, gx2, gy2, label, children in groups:
        cx = (gx1 + gx2) // 2
        clean.arrow(d, [(cx, trunk_y), (cx, gy1)], width=5)
        clean.rect(d, (gx1, gy1, gx2, gy1 + 115), label, group_font)
        clean.plain_rect(d, (gx1, gy1 + 115, gx2, gy2), width=5)
        for i, child in enumerate(children):
            y = gy1 + 160 + i * 88
            d.text((gx1 + 45, y), child, font=list_font, fill="black")
    clean.save(img, "image7_modules.png")


def diagram_er() -> None:
    img, d = clean.canvas(2300, 1260)
    name_font = clean.f(66, True)
    rel_font = clean.f(48)

    def entity(x: int, y: int, w: int, h: int, name: str) -> tuple[int, int, int, int]:
        box = (x, y, x + w, y + h)
        clean.rect(d, box, name, name_font, width=6, radius=12)
        return box

    boxes = {
        "user": entity(100, 100, 320, 120, "用户"),
        "role": entity(620, 100, 320, 120, "角色"),
        "rule": entity(1140, 100, 360, 120, "阈值规则"),
        "area": entity(100, 500, 320, 120, "库区"),
        "device": entity(620, 500, 320, 120, "设备"),
        "data": entity(1140, 500, 360, 120, "监测数据"),
        "alert": entity(1700, 500, 320, 120, "告警"),
        "ai": entity(620, 900, 320, 120, "AI 会话"),
        "msg": entity(1140, 900, 360, 120, "AI 消息"),
        "order": entity(1700, 900, 320, 120, "工单"),
    }

    def edge(a: str, side_a: str, b: str, side_b: str, label: str, via: list[tuple[int, int]] | None = None, label_at: tuple[int, int] | None = None) -> None:
        def point(box: tuple[int, int, int, int], side: str) -> tuple[int, int]:
            x1, y1, x2, y2 = box
            return {
                "r": (x2, (y1 + y2) // 2),
                "l": (x1, (y1 + y2) // 2),
                "t": ((x1 + x2) // 2, y1),
                "b": ((x1 + x2) // 2, y2),
            }[side]
        pts = [point(boxes[a], side_a)] + (via or []) + [point(boxes[b], side_b)]
        clean.line(d, pts, width=4)
        mx, my = label_at or (sum(p[0] for p in pts) // len(pts), sum(p[1] for p in pts) // len(pts))
        clean.center_text(d, (mx - 100, my - 38, mx + 100, my + 38), label, rel_font, gap=2)

    edge("user", "r", "role", "l", "N:1", label_at=(520, 160))
    edge("area", "r", "device", "l", "1:N", label_at=(520, 560))
    edge("device", "r", "data", "l", "1:N", label_at=(1040, 560))
    edge("data", "r", "alert", "l", "触发", label_at=(1600, 560))
    edge("alert", "b", "order", "t", "1:1", label_at=(1860, 760))
    edge("ai", "r", "msg", "l", "1:N", label_at=(1040, 960))
    edge("user", "l", "ai", "l", "1:N", via=[(55, 160), (55, 960), (620, 960)], label_at=(310, 960))
    edge("rule", "b", "alert", "t", "匹配", via=[(1320, 320), (1860, 320)], label_at=(1600, 320))
    clean.save(img, "image8_er.png")


def diagram_class() -> None:
    img, d = clean.canvas(2300, 1260)
    name = clean.f(54, True)
    body = clean.f(46)

    def cbox(x: int, y: int, title: str, method: str) -> tuple[int, int, int, int]:
        w = 390
        h = 190
        header_h = 88
        box = (x, y, x + w, y + h)
        clean.plain_rect(d, box, width=5)
        d.line([(x, y + header_h), (x + w, y + header_h)], fill="black", width=4)
        clean.center_text(d, (x, y, x + w, y + header_h), title, name)
        clean.center_text(d, (x, y + header_h, x + w, y + h), method, body)
        return box

    boxes = {
        "user": cbox(100, 90, "User", "login()"),
        "area": cbox(570, 90, "Area", "buildTree()"),
        "device": cbox(1040, 90, "Device", "bindArea()"),
        "data": cbox(1510, 90, "SensorData", "save()"),
        "rule": cbox(570, 500, "Rule", "match()"),
        "alert": cbox(1040, 500, "Alert", "confirm()"),
        "order": cbox(1510, 500, "WorkOrder", "dispatch()"),
        "session": cbox(570, 910, "AiSession", "create()"),
        "message": cbox(1040, 910, "AiMessage", "append()"),
    }

    def edge(a: str, b: str, label: str = "") -> None:
        av = boxes[a]
        bv = boxes[b]
        p1 = (av[2], (av[1] + av[3]) // 2) if av[0] < bv[0] else ((av[0] + av[2]) // 2, av[3])
        p2 = (bv[0], (bv[1] + bv[3]) // 2) if av[0] < bv[0] else ((bv[0] + bv[2]) // 2, bv[1])
        if p1[1] == p2[1] or p1[0] == p2[0]:
            pts = [p1, p2]
        else:
            pts = [p1, ((p1[0] + p2[0]) // 2, p1[1]), ((p1[0] + p2[0]) // 2, p2[1]), p2]
        clean.line(d, pts, width=4)
        if label:
            mx = sum(p[0] for p in pts) // len(pts)
            my = sum(p[1] for p in pts) // len(pts)
            clean.center_text(d, (mx - 70, my - 35, mx + 70, my + 35), label, body)

    for a, b in [("area", "device"), ("device", "data"), ("rule", "alert"), ("alert", "order"), ("user", "session"), ("session", "message")]:
        edge(a, b)
    edge("device", "alert")
    clean.save(img, "image9_class.png")


def diagram_ai_sequence() -> None:
    img, d = clean.canvas(2500, 1220)
    top_font = clean.f(50)
    msg_font = clean.f(40)
    xs = [150, 510, 870, 1230, 1590, 1950, 2310]
    labels = ["用户", "AI 助手页", "后端接口", "上下文服务", "数据库", "大模型接口", "消息记录"]
    for x, label in zip(xs, labels):
        clean.rect(d, (x - 132, 85, x + 132, 175), label, top_font, width=4)
        clean.dashed_line(d, (x, 175), (x, 1070), width=3, dash=24)
    messages = [
        (0, 1, 255, "输入业务问题"),
        (1, 2, 355, "提交问题"),
        (2, 3, 455, "构造分析任务"),
        (3, 4, 555, "查询监测与告警数据"),
        (4, 3, 655, "返回业务上下文"),
        (3, 5, 755, "调用兼容式接口"),
        (5, 3, 855, "返回分析结果"),
        (3, 6, 955, "保存会话消息"),
        (2, 1, 1055, "返回 Markdown 答复"),
    ]
    for a, b, y, msg in messages:
        clean.arrow(d, [(xs[a], y), (xs[b], y)], width=4, head=22)
        clean.center_text(d, (min(xs[a], xs[b]) + 10, y - 54, max(xs[a], xs[b]) - 10, y - 10), msg, msg_font)
    clean.save(img, "image13_ai_sequence.png")


def main() -> None:
    clean.diagram_roles_permissions()
    clean.diagram_business_flow()
    clean.diagram_alert_loop()
    diagram_use_case()
    diagram_architecture()
    diagram_deployment()
    diagram_modules()
    diagram_er()
    diagram_class()
    clean.diagram_login_sequence()
    clean.diagram_realtime_flow()
    clean.diagram_alert_generation()
    diagram_ai_sequence()
    clean.diagram_mini_order()
    clean.contact_sheet()


if __name__ == "__main__":
    main()
