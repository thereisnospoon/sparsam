package me.thereisnospoon.sparsam.dao;

import me.thereisnospoon.sparsam.exceptions.dao.EntityAlreadyExistsException;
import me.thereisnospoon.sparsam.vo.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/sparsam-config.xml")
public class UserDAOTest {

	private static final String TEST_USERNAME = "testuser1";
	private static final String TEST_ENCODED_PASSWORD = "1234";

	@Autowired
	private UserDAO userDAO;

	private User createTestUser() {

		User user = new User();
		user.setUsername(TEST_USERNAME);
		user.setEncodedPassword(TEST_ENCODED_PASSWORD);

		return user;
	}

	@Before
	public void setUp() {
		deleteTestUserIfExists();
	}

	private void deleteTestUserIfExists() {

		User testUser = createTestUser();
		if (userDAO.exists(testUser.getUsername())) {
			userDAO.delete(testUser);
		}
	}

	@After
	public void cleanUp() {
		deleteTestUserIfExists();
	}

	@Test
	public void testUserCreationInRedis() {

		User testUser = createTestUser();
		userDAO.create(testUser);

		User userRetrievedFromStorage = userDAO.getUserByUsername(TEST_USERNAME);

		assertEquals(TEST_USERNAME, userRetrievedFromStorage.getUsername());
		assertEquals(TEST_ENCODED_PASSWORD, userRetrievedFromStorage.getEncodedPassword());
	}

	@Test(expected = EntityAlreadyExistsException.class)
	public void testExistingUserCreationInRedis() {

		User testUser = createTestUser();
		userDAO.create(testUser);
		userDAO.create(testUser);
	}
}