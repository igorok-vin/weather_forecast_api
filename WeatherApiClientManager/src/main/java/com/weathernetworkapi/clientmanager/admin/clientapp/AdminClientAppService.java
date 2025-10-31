package com.weathernetworkapi.clientmanager.admin.clientapp;

import java.util.Optional;

import com.skyapi.weathernetworkapi.common.clientmanager.AppRole;
import com.skyapi.weathernetworkapi.common.clientmanager.ClientApp;
import com.skyapi.weathernetworkapi.common.clientmanager.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.weathernetworkapi.clientmanager.ClientAppNotFoundException;
import com.weathernetworkapi.clientmanager.admin.user.UserNotFoundException;
import com.weathernetworkapi.clientmanager.admin.user.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AdminClientAppService {
	static final int APPS_PER_PAGE = 5;
	private AdminClientAppRepository appRepo;
	private UserRepository userRepo;
	private PasswordEncoder passwordEncoder;

	@Autowired
	public AdminClientAppService(AdminClientAppRepository appRepo, UserRepository userRepo, PasswordEncoder passwordEncoder) {
		this.appRepo = appRepo;
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
	}
	
	public Page<ClientApp> listByPage(int pageNum, String sortField, String sortDir, String keyword) {
		Sort sort = Sort.by(sortField);
		
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
			
		PageRequest pageRequest = PageRequest.of(pageNum - 1, APPS_PER_PAGE, sort);
		
		if (keyword != null) {
			return appRepo.findAll(keyword, pageRequest);
		}
		
		return appRepo.findAll(pageRequest);
	}	
	
	public void updateAppEnabledStatus(Integer id, boolean enabled) throws ClientAppNotFoundException {
		if (!appRepo.existsById(id)) {
			throw new ClientAppNotFoundException("Could not find app with ID " + id);
		}		
		appRepo.updateEnabledStatus(id, enabled);
	}		
	
	public ClientApp save(String userEmail, ClientApp app) throws UserNotFoundException {
		
		User user = userRepo.findByEmailEnabledAndNotTrashed(userEmail);
		
		if (user == null) {
			throw new UserNotFoundException("No valid user found with the given email " + userEmail);
		}
		
		return app.isEditing() ? updateExistingApp(app, user) : saveNewApp(app, user);		
	}

	private ClientApp updateExistingApp(ClientApp appInForm, User user) {
		ClientApp appInDB = appRepo.findById(appInForm.getId()).get();
		appInDB.setName(appInForm.getName());
		appInDB.setEnabled(appInForm.isEnabled());
		
		if (appInDB.getRole().equals(AppRole.UPDATER) &&
				!appInForm.getRole().equals(AppRole.UPDATER)) {
			appInDB.setLocation(null);
		} else if (appInForm.getRole().equals(AppRole.UPDATER)) {
			appInDB.setLocation(appInForm.getLocation());
		}
		
		appInDB.setRole(appInForm.getRole());
		appInDB.setUser(user);
		
		return appRepo.save(appInDB);
	}
	
	private ClientApp saveNewApp(ClientApp app, User user) {
		String clientId = RandomStringUtils.randomAlphanumeric(20);
		String clientSecret = RandomStringUtils.randomAlphanumeric(40);
		
		app.setClientId(clientId);
		
		String encodedSecret = passwordEncoder.encode(clientSecret);
		app.setClientSecret(encodedSecret);
		app.setUser(user);
		
		ClientApp savedApp = appRepo.save(app);
		savedApp.setRawClientSecret(clientSecret);
		
		return savedApp;
	}	
	
	public ClientApp get(Integer id) throws ClientAppNotFoundException {
		Optional<ClientApp> result = appRepo.findByIdNotTrashed(id);
		if (result.isEmpty()) {
			throw new ClientAppNotFoundException("No app found with the given id: " + id);
		}
		
		return result.get();
	}

	public void delete(Integer id) throws ClientAppNotFoundException {
		if (!appRepo.existsById(id)) {
			throw new ClientAppNotFoundException("Could not find app with ID " + id);
		}
		
		appRepo.trashById(id);
	}
}
