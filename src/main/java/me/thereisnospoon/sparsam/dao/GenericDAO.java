package me.thereisnospoon.sparsam.dao;

public interface GenericDAO<T> {

	void setRedisCollectionNameForEntities(final String redisCollectionName);

	T find(final String key);

	boolean exists(final String key);

	void create(final T entity);

	void delete(final String key);
}
