package id.my.berviantoleo.ecceg_rsa_app.lib.ecc

class Pair<L, R>(var left: L?, var right: R?) {
    override fun hashCode(): Int {
        return left.hashCode() xor right.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Pair<*, *>) return false
        val pair = other
        return left == pair.left && right == pair.right
    }

    override fun toString(): String {
        return "<$left, $right>"
    }
}