# GD656KillIcon 1.21.11 移植版

原仓库：https://github.com/MinecraftGD656/gd656killicon
原作者：Minecraft_GD656

基于 1.20.1 的 1.0.0RC5 移植到 1.21.11，功能没动，只做了版本适配。

需要 Fabric Loader 0.19.5 以上、Fabric API 和 Java 21，Mod Menu 可装可不装。

## 服务端没装的时候

原来服务端没装本 mod 的话什么都不会显示。移植版加了个客户端本地判定，
只在检测到服务端没有本 mod 的时候才启用，所以装了本 mod 的服务器不受影响，
不会重复弹。

能判定到的只有近战和自己射出去的箭、三叉戟。近战就是看 5 秒内有没有打过
这个生物，暴击照原版条件算；远程的话死亡点附近 4 格内有属于自己的弹射物
就算。但因为做的是移植所以矛还没有进行适配。分数、助攻、远距离、魔法伤害还有别的 mod 的枪械弹丸这些判定不了，
本来就得靠服务端。

## 适配过程中碰到的坑

记一下 1.20.1 到 1.21.11 之间改掉的地方，方便以后别的版本移植参考。

GUI 的矩阵从 MatrixStack 换成了 JOML 的 Matrix3x2fStack，push/pop、
translate、scale、旋转的写法全变了。鼠标事件统一成 Click 记录类型。
DrawContext.drawBorder 没了，边框自己拿四次 fill 拼。聊天组件的点击和悬停
事件改成了 ClickEvent.RunCommand 和 HoverEvent.ShowText。

许可沿用原仓库的 MIT。
