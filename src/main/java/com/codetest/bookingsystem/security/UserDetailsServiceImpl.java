package com.codetest.bookingsystem.security;

import com.codetest.bookingsystem.model.AppUser;
import com.codetest.bookingsystem.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private AppUserRepository userRepository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
		// Allow login with either username or email
		AppUser user = userRepository.findByUsername(usernameOrEmail).orElseGet(
				() -> userRepository.findByEmail(usernameOrEmail).orElseThrow(() -> new UsernameNotFoundException(
						"User Not Found with username or email: " + usernameOrEmail)));

		return UserDetailsImpl.build(user);
	}
}