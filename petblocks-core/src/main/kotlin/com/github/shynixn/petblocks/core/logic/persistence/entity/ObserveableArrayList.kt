package com.github.shynixn.petblocks.core.logic.persistence.entity

/**
 * Created by Shynixn 2019.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2019 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class ObserveableArrayList<E>(private val hasChanged: () -> Unit) : MutableList<E> {
    private val backedArrayList = ArrayList<E>()

    override val size: Int
        get() = backedArrayList.size

    override fun contains(element: E): Boolean {
        return backedArrayList.contains(element)
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return backedArrayList.containsAll(elements)
    }

    /**
     * Returns the element at the specified index in the list.
     */
    override fun get(index: Int): E {
        return backedArrayList[index]
    }

    /**
     * Returns the index of the first occurrence of the specified element in the list, or -1 if the specified
     * element is not contained in the list.
     */
    override fun indexOf(element: E): Int {
        return backedArrayList.indexOf(element)
    }

    override fun isEmpty(): Boolean {
        return backedArrayList.isEmpty()
    }

    override fun iterator(): MutableIterator<E> {
        return backedArrayList.iterator()
    }


    override fun listIterator(): MutableListIterator<E> {
        return backedArrayList.listIterator()
    }

    override fun listIterator(index: Int): MutableListIterator<E> {
        return backedArrayList.listIterator(index)
    }


    override fun retainAll(elements: Collection<E>): Boolean {
        return backedArrayList.retainAll(elements)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        return backedArrayList.subList(fromIndex, toIndex)
    }

    /**
     * Returns the index of the last occurrence of the specified element in the list, or -1 if the specified
     * element is not contained in the list.
     */
    override fun lastIndexOf(element: E): Int {
        return backedArrayList.lastIndexOf(element)
    }

    /**
     * Adds the specified element to the end of this list.
     *
     * @return `true` because the list is always modified as the result of this operation.
     */
    override fun add(element: E): Boolean {
        val item = backedArrayList.add(element)
        this.hasChanged.invoke()
        return item
    }

    /**
     * Inserts an element into the list at the specified [index].
     */
    override fun add(index: Int, element: E) {
        backedArrayList.add(index, element)
        this.hasChanged.invoke()
    }

    /**
     * Inserts all of the elements of the specified collection [elements] into this list at the specified [index].
     *
     * @return `true` if the list was changed as the result of the operation.
     */
    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        val item = backedArrayList.addAll(index, elements)

        if (item) {
            this.hasChanged.invoke()
        }

        return item
    }

    /**
     * Adds all of the elements of the specified collection to the end of this list.
     *
     * The elements are appended in the order they appear in the [elements] collection.
     *
     * @return `true` if the list was changed as the result of the operation.
     */
    override fun addAll(elements: Collection<E>): Boolean {
        val item = backedArrayList.addAll(elements)

        if (item) {
            this.hasChanged.invoke()
        }

        return item
    }

    /**
     * Adds all of the elements of the specified collection to the end of this list.
     *
     * The elements are appended in the order they appear in the [elements] collection.
     *
     * @return `true` if the list was changed as the result of the operation.
     */
    fun addAllWithoutChangeTrigger(elements: Collection<E>): Boolean {
        return backedArrayList.addAll(elements)
    }

    override fun clear() {
        backedArrayList.clear()
        this.hasChanged.invoke()
    }

    override fun remove(element: E): Boolean {
        val item = backedArrayList.remove(element)
        this.hasChanged.invoke()
        return item
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        val item = backedArrayList.removeAll(elements)

        if (item) {
            this.hasChanged.invoke()
        }

        return item
    }

    /**
     * Removes an element at the specified [index] from the list.
     *
     * @return the element that has been removed.
     */
    override fun removeAt(index: Int): E {
        val item = backedArrayList.removeAt(index)
        this.hasChanged.invoke()
        return item
    }

    /**
     * Replaces the element at the specified position in this list with the specified element.
     *
     * @return the element previously at the specified position.
     */
    override fun set(index: Int, element: E): E {
        val item = backedArrayList.set(index, element)
        this.hasChanged.invoke()
        return item
    }
}