package me.thereisnospoon.sparsam.dao.impl;

import com.google.common.base.Preconditions;
import me.thereisnospoon.sparsam.dao.UserDAO;
import me.thereisnospoon.sparsam.exceptions.dao.EntityAlreadyExistsException;
import me.thereisnospoon.sparsam.vo.User;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.util.StringUtils;

public class UserDAOImpl implements UserDAO {

	private static final String ERROR_MESSAGE_FOR_USERNAME_EMPTY_CHECK = "'username' cannot be empty";

	private String redisCollectionNameForEntities;
	private HashOperations<String,String,User> redisHashOperations;

	public void setRedisHashOperations(HashOperations<String, String, User> redisHashOperations) {
		this.redisHashOperations = redisHashOperations;
	}

	public void setRedisCollectionNameForEntities(String redisCollectionNameForEntities) {
		this.redisCollectionNameForEntities = redisCollectionNameForEntities;
	}

	@Override
	public User getUserByUsername(final String username) {
		return redisHashOperations.get(redisCollectionNameForEntities, username);
	}

	@Override
	public boolean exists(final User user) {
		return redisHashOperations.hasKey(redisCollectionNameForEntities, user.getUsername());
	}

	private void checkIfUserValidForStorage(final User user) {

		Preconditions.checkNotNull(user);
		Preconditions.checkArgument(!StringUtils.isEmpty(user.getUsername()), ERROR_MESSAGE_FOR_USERNAME_EMPTY_CHECK);
	}

	@Override
	public void create(final User user) {

		checkIfUserValidForStorage(user);
		if (exists(user)) {
			throw new EntityAlreadyExistsException(String.format("User with name: '%s' already exists", user.getUsername()));
		}

		redisHashOperations.put(redisCollectionNameForEntities, user.getUsername(), user);
	}

	@Override
	public void delete(final User user) {

		Preconditions.checkArgument(!StringUtils.isEmpty(user.getUsername()), ERROR_MESSAGE_FOR_USERNAME_EMPTY_CHECK);
		redisHashOperations.delete(redisCollectionNameForEntities, user.getUsername());
	}
}
