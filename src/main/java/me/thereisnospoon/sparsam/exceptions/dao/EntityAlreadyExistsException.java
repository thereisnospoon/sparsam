package me.thereisnospoon.sparsam.exceptions.dao;

public class EntityAlreadyExistsException extends RuntimeException {

	public EntityAlreadyExistsException(final String message) {
		super(message);
	}
}
