package com.example.bervianto.ecceg_rsa_app.ecc;

import androidx.annotation.NonNull;

public class Pair<L, R> {
    public L left;
    public R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public int hashCode() {
        return left.hashCode() ^ right.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) return false;
        Pair pair = (Pair) o;
        return left.equals(pair.left) && right.equals(pair.right);
    }

    @NonNull
    @Override
    public String toString() {
        return "<" + left.toString() + ", " + right.toString() + ">";
    }
}