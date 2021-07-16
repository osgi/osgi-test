package org.osgi.test.common.list;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class ListSupplierDelegate<E> implements List<E> {

	private final Supplier<? extends List<E>> supplier;

	public ListSupplierDelegate(Supplier<? extends List<E>> supplier) {
		this.supplier = Objects.requireNonNull(supplier);
	}

	@Override
	public boolean add(E e) {
		return supplier.get()
			.add(e);
	}

	@Override
	public void add(int index, E element) {
		supplier.get()
			.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return supplier.get()
			.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		return supplier.get()
			.addAll(index, c);
	}

	@Override
	public void clear() {
		supplier.get()
			.clear();
	}

	@Override
	public boolean contains(Object o) {
		return supplier.get()
			.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return supplier.get()
			.containsAll(c);
	}

	@Override
	public boolean equals(Object o) {
		return supplier.get()
			.equals(o);
	}

	@Override
	public void forEach(Consumer<? super E> action) {
		supplier.get()
			.forEach(action);
	}

	@Override
	public E get(int index) {
		return supplier.get()
			.get(index);
	}

	@Override
	public int hashCode() {
		return supplier.get()
			.hashCode();
	}

	@Override
	public int indexOf(Object o) {
		return supplier.get()
			.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return supplier.get()
			.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return supplier.get()
			.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return supplier.get()
			.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return supplier.get()
			.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return supplier.get()
			.listIterator(index);
	}

	@Override
	public Stream<E> parallelStream() {
		return supplier.get()
			.parallelStream();
	}

	@Override
	public E remove(int index) {
		return supplier.get()
			.remove(index);
	}

	@Override
	public boolean remove(Object o) {
		return supplier.get()
			.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return supplier.get()
			.removeAll(c);
	}

	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		return supplier.get()
			.removeIf(filter);
	}

	@Override
	public void replaceAll(UnaryOperator<E> operator) {
		supplier.get()
			.replaceAll(operator);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return supplier.get()
			.retainAll(c);
	}

	@Override
	public E set(int index, E element) {
		return supplier.get()
			.set(index, element);
	}

	@Override
	public int size() {
		return supplier.get()
			.size();
	}

	@Override
	public void sort(Comparator<? super E> c) {
		supplier.get()
			.sort(c);
	}

	@Override
	public Spliterator<E> spliterator() {
		return supplier.get()
			.spliterator();
	}

	@Override
	public Stream<E> stream() {
		return supplier.get()
			.stream();
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return supplier.get()
			.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return supplier.get()
			.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return supplier.get()
			.toArray(a);
	}
}
