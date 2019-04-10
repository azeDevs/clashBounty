package classes

import kotlinx.cinterop.convert
import libui.*
import libui.ktx.*
import libui.ktx.draw.*


/**
 * ------------------------------------------------------------------------
 * ======== LIBUI SAMPLE ==================================================
 * URL: https://github.com/msink/kotlin-libui/tree/master/samples/hello
 * SAMPLE: [HelloWorld]
 * ------------------------------------------------------------------------
 */

fun libuiHello() = appWindow(title = "Hello", width = 320, height = 240) {
    vbox { lateinit var scroll: TextArea

        button("hey you: click me!") { action {
            scroll.append("""
            |Hello, World!  Ciao, mondo!
            |Привет, мир!  你好，世界！
            |
            |""".trimMargin())
        } }
        scroll = textarea {
            readonly = true
            stretchy = true
        }
    }
}


/**
 * ------------------------------------------------------------------------
 * ======== LIBUI SAMPLE ==================================================
 * URL: https://github.com/msink/kotlin-libui/tree/master/samples/hello
 * SAMPLE: [Histogram]
 * ------------------------------------------------------------------------
 */

// histogram margins
private const val xoffLeft = 20.0
private const val yoffTop = 20.0
private const val xoffRight = 20.0
private const val yoffBottom = 20.0
private const val pointRadius = 5.0
// and some colors
private const val colorWhite = 0xFFFFFF
private const val colorBlack = 0x000000
private const val colorDodgerBlue = 0x1E90FF
// graph dimensions
private fun graphWidth(clientWidth: Double): Double = clientWidth - xoffLeft - xoffRight
private fun graphHeight(clientHeight: Double): Double = clientHeight - yoffTop - yoffBottom

private const val numPoints = 10

fun libuiHistogram() = appWindow(
    title = "libui Histogram Example",
    width = 640,
    height = 480
) {
    hbox {
        lateinit var datapoints: Array<Spinbox>
        lateinit var colorButton: ColorButton
        lateinit var histogram: DrawArea
        var currentPoint = -1

        fun pointLocations(width: Double, height: Double, xs: DoubleArray, ys: DoubleArray) {
            val xincr = width / (numPoints - 1)  // to make the last point be at the end
            val yincr = height / 100
            repeat(numPoints) { i ->
                // because y=0 is the top but n=0 is the bottom, we need to flip
                val n = 100 - datapoints[i].value
                xs[i] = xincr * i
                ys[i] = yincr * n
            }
        }

        vbox {
            datapoints = Array(numPoints) {
                spinbox(0, 100) {
                    value = random() % 101
                    action {
                        histogram.redraw()
                    }
                }
            }
            colorButton = colorbutton {
                value = Color(colorDodgerBlue)
                action {
                    histogram.redraw()
                }
            }
        }

        histogram = drawarea {
            stretchy = true

            val brush = brush()

            // make a stroke for both the axes and the histogram line
            val stroke = stroke {
                Cap = uiDrawLineCapFlat
                Join = uiDrawLineJoinMiter
                Thickness = 2.0
                MiterLimit = uiDrawDefaultMiterLimit
            }

            draw {
                val graphWidth = graphWidth(it.AreaWidth)
                val graphHeight = graphHeight(it.AreaHeight)
                val graphColor = colorButton.value
                val xs = DoubleArray(numPoints)
                val ys = DoubleArray(numPoints)
                pointLocations(graphWidth, graphHeight, xs, ys)

                // fill the area with white
                fill(brush.solid(colorWhite)) {
                    rectangle(0.0, 0.0, it.AreaWidth, it.AreaHeight)
                }

                // draw the axes
                stroke(brush.solid(colorBlack), stroke) {
                    figure(xoffLeft, yoffTop)
                    lineTo(xoffLeft, yoffTop + graphHeight)
                    lineTo(xoffLeft + graphWidth, yoffTop + graphHeight)
                }

                // transform the coordinate space so (0, 0) is the top-left corner of the graph
                transform {
                    translate(xoffLeft, yoffTop)
                }

                // create the fill for the graph below the graph line
                fill(brush.solid(graphColor, opacity = 0.5)) {
                    figure(xs[0], ys[0])
                    for (i in 1 until numPoints)
                        lineTo(xs[i], ys[i])
                    lineTo(graphWidth, graphHeight)
                    lineTo(0.0, graphHeight)
                    closeFigure()
                }

                // draw the histogram line
                stroke(brush.solid(graphColor), stroke) {
                    figure(xs[0], ys[0])
                    for (i in 1 until numPoints)
                        lineTo(xs[i], ys[i])
                }

                // draw the point being hovered over
                if (currentPoint != -1) {
                    fill(brush) {
                        figureWithArc(
                            xs[currentPoint], ys[currentPoint], pointRadius,
                            startAngle = 0.0, sweep = 6.23
                        )
                    }
                }
            }

            mouseEvent {
                val graphWidth = graphWidth(it.AreaWidth)
                val graphHeight = graphHeight(it.AreaHeight)
                val x = it.X - xoffLeft
                val y = it.Y - yoffTop
                val xs = DoubleArray(numPoints)
                val ys = DoubleArray(numPoints)
                pointLocations(graphWidth, graphHeight, xs, ys)

                currentPoint = -1
                repeat(numPoints) { i ->
                    if ((x >= xs[i] - pointRadius) &&
                        (x <= xs[i] + pointRadius) &&
                        (y >= ys[i] - pointRadius) &&
                        (y <= ys[i] + pointRadius)
                    ) {
                        currentPoint = i
                        return@repeat
                    }
                }

                redraw()
            }
        }
    }
}

/**
 * ------------------------------------------------------------------------
 * ======== LIBUI SAMPLE ==================================================
 * URL: https://github.com/msink/kotlin-libui/tree/master/samples/hello
 * SAMPLE: [DrawFont]
 * ------------------------------------------------------------------------
 */

fun AttributedString.append(what: String, attr: Attribute, attr2: Attribute? = null) {
    val start = length
    val end = start + what.length
    append(what)
    setAttribute(attr, start, end)
    if (attr2 != null)
        setAttribute(attr2, start, end)
}

fun DrawArea.makeAttributedString() = string(
    "Drawing strings with libui is done with the uiAttributedString and uiDrawTextLayout objects.\n" +
            "uiAttributedString lets you have a variety of attributes: "
).apply {
    append("font family", FamilyAttribute("Courier New"))
    append(", ")
    append("font size", SizeAttribute(18.0))
    append(", ")
    append("font weight", WeightAttribute(uiTextWeightBold))
    append(", ")
    append("font italicness", ItalicAttribute(uiTextItalicItalic))
    append(", ")
    append("font stretch", StretchAttribute(uiTextStretchCondensed))
    append(", ")
    append("text color", ColorAttribute(Color(r = 0.75, g = 0.25, b = 0.5, a = 0.75)))
    append(", ")
    append("text background color", BackgroundAttribute(Color(r = 0.5, g = 0.5, b = 0.25, a = 0.5)))
    append(", ")
    append("underline style", UnderlineAttribute(uiUnderlineSingle))
    append(", ")
    append("and ")
    append(
        "underline color",
        UnderlineAttribute(uiUnderlineDouble),
        UnderlineColorAttribute(uiUnderlineColorCustom, Color(r = 1.0, g = 0.0, b = 0.5, a = 1.0))
    )
    append(" ")
    append("Furthermore, there are attributes allowing for ")
    append(
        "special underlines for indicating spelling errors",
        UnderlineAttribute(uiUnderlineSuggestion),
        UnderlineColorAttribute(uiUnderlineColorSpelling, Color(r = 0.0, g = 0.0, b = 0.0, a = 0.0))
    )
    append(" (and other types of errors) ")
    append("and control over OpenType features such as ligatures (for instance, ")

    val otf = OpenTypeFeatures()
    otf.add("liga", 0u)
    append("afford", FeaturesAttribute(otf))
    append(" vs. ")
    otf.add("liga", 1u)
    append("afford", FeaturesAttribute(otf))
    otf.dispose()
    append(").\n")

    append("Use the controls opposite to the text to control properties of the text.")
}

fun libuiDrawFont() = appWindow(
    title = "libui Text-Drawing Example",
    width = 640,
    height = 480
) {
    hbox {
        lateinit var font: FontButton
        lateinit var align: Combobox
        lateinit var area: DrawArea

        vbox {
            font = fontbutton {
                action { area.redraw() }
            }
            form {
                align = combobox {
                    label = "Alignment"
                    item("Left")
                    item("Center")
                    item("Right")
                    value = 0
                    action { area.redraw() }
                }
            }
        }
        area = drawarea {
            val str = makeAttributedString()
            draw {
                text(str, font.value, it.AreaWidth, align.value.convert(), 0.0, 0.0)
            }
            stretchy = true
        }
    }
}