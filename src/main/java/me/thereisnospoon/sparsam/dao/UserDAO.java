package me.thereisnospoon.sparsam.dao;

import com.google.common.base.Preconditions;
import me.thereisnospoon.sparsam.exceptions.dao.EntityAlreadyExistsException;
import me.thereisnospoon.sparsam.vo.User;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.util.StringUtils;

public class UserDAO implements GenericDAO<User> {

	private static final String ERROR_MESSAGE_FOR_USERNAME_EMPTY_CHECK = "'username' cannot be empty";

	private String redisCollectionNameForEntities;
	private HashOperations<String,String,User> redisHashOperations;

	@Override
	public void setRedisCollectionNameForEntities(String redisCollectionName) {
		this.redisCollectionNameForEntities = redisCollectionName;
	}

	public void setRedisHashOperations(HashOperations<String, String, User> redisHashOperations) {
		this.redisHashOperations = redisHashOperations;
	}

	@Override
	public User find(final String username) {
		return redisHashOperations.get(redisCollectionNameForEntities, username);
	}

	@Override
	public boolean exists(final String username) {
		return redisHashOperations.hasKey(redisCollectionNameForEntities, username);
	}

	private void checkIfUserValidForStorage(final User user) {

		Preconditions.checkNotNull(user);
		Preconditions.checkArgument(!StringUtils.isEmpty(user.getUsername()), ERROR_MESSAGE_FOR_USERNAME_EMPTY_CHECK);
	}

	@Override
	public void create(final User user) {

		checkIfUserValidForStorage(user);
		if (exists(user.getUsername())) {
			throw new EntityAlreadyExistsException(String.format("User with name: '%s' already exists", user.getUsername()));
		}

		redisHashOperations.put(redisCollectionNameForEntities, user.getUsername(), user);
	}

	@Override
	public void delete(final String username) {

		Preconditions.checkArgument(!StringUtils.isEmpty(username), ERROR_MESSAGE_FOR_USERNAME_EMPTY_CHECK);
		redisHashOperations.delete(redisCollectionNameForEntities, username);
	}
}
