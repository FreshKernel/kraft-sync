package utils

// TODO: Currently has two problems, this will make it inefficient to support CLI mode and localizations
//  and it needs some refactoring too.

class HtmlBuilder {
    private val content = StringBuilder()

    fun buildBodyAsText(): String = "<html><body>$content</body></html>"

    fun buildAsText(): String = "<html>$content</html>"

    fun center(block: HtmlBuilder.() -> Unit) {
        content.append("<div align='center'>")
        this.block()
        content.append("</div>")
    }

    fun centerWithCss(block: HtmlBuilder.() -> Unit) {
        content.append("<div style='text-align:center;'>")
        this.block()
        content.append("</div>")
    }

    fun link(
        labelText: String,
        linkUrl: String,
    ) {
        content.append("<a href='$linkUrl'>$labelText</a>")
    }

    fun text(text: String) {
        content.append(text)
    }

    fun boldText(text: String) {
        content.append("<b>$text</b>")
    }

    fun newLine() {
        content.append("<br>")
    }

    fun newLines(numberOfLines: Int) {
        for (numberOfLine in 1..numberOfLines) {
            newLine()
        }
    }
}

fun buildHtml(block: HtmlBuilder.() -> Unit): HtmlBuilder {
    return HtmlBuilder().apply(block)
}
