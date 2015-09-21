package me.thereisnospoon.sparsam.dao;

import me.thereisnospoon.sparsam.vo.User;

public interface UserDAO {

	User getUserByUsername(final String username);

	boolean exists(final User user);

	void create(final User user);

	void delete(final User user);
}
