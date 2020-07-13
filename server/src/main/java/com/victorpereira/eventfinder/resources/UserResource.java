package com.victorpereira.eventfinder.resources;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.victorpereira.eventfinder.dto.EventDTO;
import com.victorpereira.eventfinder.models.Event;
import com.victorpereira.eventfinder.models.User;
import com.victorpereira.eventfinder.repositories.EventRepository;
import com.victorpereira.eventfinder.repositories.UserRepository;
import com.victorpereira.eventfinder.resources.exceptions.ObjectNotFoundException;
import com.victorpereira.eventfinder.resources.utils.Utils;
import com.victorpereira.eventfinder.services.EventService;
import com.victorpereira.eventfinder.services.UserService;

@RestController
@RequestMapping(value = "/users")
public class UserResource {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private UserService userService;

	@Autowired
	private EventRepository eventRepo;
	
	@Autowired
	private EventService eventService;

	// Only for testing
	@GetMapping()
	public List<User> findAll() {
		return userRepo.findAll();
	}

	@GetMapping(value = "/{id}")
	public User findById(@PathVariable Integer id) {
		Optional<User> user = userRepo.findById(id);
		return user.orElseThrow(
				() -> new ObjectNotFoundException("Object not found! Id: " + id + ", Tipo: " + User.class.getName()));
	}

	@PostMapping
	public User insert(@RequestBody User user) {
		return userRepo.save(user);
	}

	@DeleteMapping(value = "/{id}")
	public void delete(@PathVariable Integer id) {
		User user = findById(id);
		userRepo.delete(user);
	}

	@PutMapping(value = "/{id}")
	public User update(@RequestBody User user, @PathVariable Integer id) {
		User obj = findById(id);
		obj = Utils.validateUserFields(user, obj);

		return userRepo.save(obj);
	}

	// Create event via user
	@PostMapping(value = "/{id}/events")
	public Event createEvent(@RequestBody Event event, @PathVariable Integer id) {
		User user = findById(id);
		event.setOwner(user);
		return eventRepo.save(event);
	}

	// Get events from a user
	@GetMapping(value = "/{id}/events")
	public List<EventDTO> listEvents(@PathVariable Integer id) {
		List<Event> list = eventRepo.findEvents(id);
		List<EventDTO> listDto = list.stream().map(obj -> new EventDTO(obj)).collect(Collectors.toList());
		for (EventDTO x : listDto) {
			x.setOwner(userService.toDto(findById(id)));
		}
		return listDto;
	}

	// Delete an event via user
	@DeleteMapping(value = "/{id}/events/{event_id}")
	public void deleteEvent(@PathVariable Integer id, @PathVariable Integer event_id) {
		List<EventDTO> events = listEvents(id);
		for (EventDTO e : events) {
			if (e.getId() == event_id) {
				eventRepo.deleteById(event_id);
				break;
			}
		}
	}

	// Updating an event via user
	@PutMapping(value = "/{id}/events/{event_id}")
	public Event updateEvent(@RequestBody Event event, @PathVariable Integer id, @PathVariable Integer event_id) {
		List<EventDTO> events = listEvents(id);
		for (EventDTO e : events) {
			if (e.getId() == event_id) {
				Event obj = eventService.toEvent(e);
				obj.setOwner(findById(id));
				obj = Utils.validateEventFields(event, obj);
				return eventRepo.save(obj);
			}
		}
		return null;
	}
	
	// Get subscribed events of a user
	@GetMapping(value="/{id}/subevents") 
	public List<Event> subscribedEvents(@PathVariable Integer id) {
		List<Event> list = eventRepo.findAll();
		return eventRepo.subscribedEvents(list, findById(id));
	}
	
	// Delete a subscribed event of a user
	@DeleteMapping(value="/{id}/subevents/{event_id}")
	public void deleteSubscribedEvent(@PathVariable Integer id, @PathVariable Integer event_id) {
		List<Event> list = eventRepo.subscribedEvents(eventRepo.findAll(), findById(id));
		for(Event x : list) {
			if(x.getId() == event_id) {
				eventRepo.deleteById(event_id);
				break;
			}
		}
	}
}
