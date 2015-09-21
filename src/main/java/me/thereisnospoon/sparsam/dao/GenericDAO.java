package me.thereisnospoon.sparsam.dao;

public interface GenericDAO<T> {

	T getEntryByKey(final String key);

	boolean exists(final T entry);

	void create(final T entity);

	void delete(final T entity);
}
