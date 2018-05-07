package com.mountainpier.auth.service;

import com.mountainpier.auth.domain.User;
import com.mountainpier.auth.exception.UserCredentialsException;
import com.mountainpier.auth.repository.UserRepository;
import com.mountainpier.auth.web.model.UserRequest;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.persistence.EntityNotFoundException;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
	
	private final UserRepository userRepository;
	
	@Autowired
	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	@Override
	public User findByUsername(String username) {
		return userRepository.findUserByUsername(username)
			.orElseThrow(() -> new EntityNotFoundException("User with username = " + username + " not found"));
	}
	
	@Override
	public User checkCredentials(String username, String password) throws UserCredentialsException {
		try {
			User user = userRepository.findUserByUsername(username)
				.orElseThrow(() -> new EntityNotFoundException("User with username = " + username + " not found"));
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
			messageDigest.update((password + user.getSalt()).getBytes("UTF-8"));
			return DatatypeConverter.printHexBinary(messageDigest.digest()).equals(user.getPassword()) ? user : null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new UserCredentialsException();
		}
	}
	
	@Override
	public User save(UserRequest userRequest) throws Exception {
		String salt = RandomStringUtils.randomAlphanumeric(10);
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
		messageDigest.update((userRequest.getPassword() + salt).getBytes("UTF-8"));
		User user = new User()
			.setId(UUID.fromString(userRequest.getId()))
			.setUsername(userRequest.getUsername())
			.setPassword(DatatypeConverter.printHexBinary(messageDigest.digest()))
			.setSalt(salt)
			.setRole(userRequest.getRole());
		return userRepository.save(user);
	}
	
	@Override
	public void delete(Integer id) {
		userRepository.deleteById(id);
	}
	
}
