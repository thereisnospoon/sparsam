package me.thereisnospoon.sparsam.dao;

import me.thereisnospoon.sparsam.vo.User;

public interface UserDAO {

	User getUserByUsername(String username);

	boolean exists(String username);

	void create(User user);

	void delete(User user);
}
