package gui.utils

data class ComboItem<T>(
    val value: T,
    val label: String,
) {
    override fun toString(): String = label
}
