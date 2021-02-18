package com.ajmpcode.springboot.app.controllers;


import java.io.IOException;
import java.util.Collection;
//import java.util.List;
//import java.net.MalformedURLException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ajmpcode.springboot.app.models.entity.Cliente;
import com.ajmpcode.springboot.app.models.service.IClienteService;
import com.ajmpcode.springboot.app.models.service.IUploadFileService;
import com.ajmpcode.springboot.app.util.PageRender;

@Controller
@SessionAttributes("cliente")
public class ClienteController {
	
	protected final Log logger = LogFactory.getLog(this.getClass());

	@Autowired
	private IClienteService clienteService;
	
	@Autowired
	private IUploadFileService uploadFileService;

	//@Secured("ROLE_USER")
	/*@GetMapping(value = "/uploads/{filename:.+}")
	public ResponseEntity<Resource> verFoto(@PathVariable String filename) {
		
		Resource recurso = null;
		try {
			recurso = uploadFileService.load(filename);
		} catch (MalformedURLException e) {		
			e.printStackTrace();
		}
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
				.body(recurso);
	}*/

	@Secured("ROLE_USER")
	@GetMapping(value = "/ver/{id}")
	public String ver(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {
		//Cliente cliente = clienteService.findOne(id);
		Cliente cliente = clienteService.fetchByIdWithFacturas(id);
		if (cliente == null) {
			flash.addFlashAttribute("error", "El Cliente no existe en la base de datos");
			return "redirect:/listar";
		}
		model.put("cliente", cliente);
		model.put("titulo", "Detalle cliente: " + cliente.getNombre());

		return "ver";
	}
	
	/*@RequestMapping(value = "/listarRest", method = RequestMethod.GET)
	public @ResponseBody List<Cliente> listarRest() {
		return clienteService.findAll();
	}*/

	@RequestMapping(value = {"/listar", "/"}, method = RequestMethod.GET)
	public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model,
			Authentication authentication) {
		
		if(authentication != null) {
			logger.info("Bienvenido: ".concat(authentication.getName()));
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(hasRole("ROLE_ADMIN")) {
			logger.info("Holis".concat(auth.getName()).concat(" tienes acceso"));
		}else {
			logger.info("Holis".concat(auth.getName()).concat(" NO tienes acceso"));
		}

		Pageable pageRequest = PageRequest.of(page, 5);
		Page<Cliente> clientes = clienteService.findAll(pageRequest);

		PageRender<Cliente> pageRender = new PageRender<>("/listar", clientes);

		model.addAttribute("titulo", "Listado de Clientes");
		model.addAttribute("clientes", clientes);
		model.addAttribute("page", pageRender);
		return "listar";
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(value = "/form")
	public String crear(Map<String, Object> model) {
		Cliente cliente = new Cliente();
		model.put("cliente", cliente);
		model.put("titulo", "Formulario de Cliente");
		return "form";
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(value = "/form/{id}")
	public String editar(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {
		Cliente cliente = null;

		if (id > 0) {
			cliente = clienteService.findOne(id);
			if (cliente == null) {
				flash.addFlashAttribute("error", "El ID del cliente no existe en la Base de Datos");
				return "redirect:/listar";
			}
		} else {
			flash.addFlashAttribute("error", "El ID del cliente no puede ser cero");
			return "redirect:/listar";
		}
		model.put("cliente", cliente);
		model.put("titulo", "Editar Cliente");
		return "form";
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(value = "/form", method = RequestMethod.POST)
	public String guardar(@Validated Cliente cliente, BindingResult result, Model model,
			@RequestParam("file") MultipartFile foto, RedirectAttributes flash, SessionStatus status) {
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Formulario de Cliente");
			return "form";
		}
		if (!foto.isEmpty()) { 

			if (cliente.getId() != null && cliente.getId() > 0 && cliente.getFoto() != null	&& cliente.getFoto().length() > 0) {
				
				uploadFileService.delete(cliente.getFoto());
			}
			
			String uniqueFilename = null;
			try {
				uniqueFilename = uploadFileService.copy(foto);
			} catch (IOException e) {
				e.printStackTrace();
			}

			flash.addFlashAttribute("info", "Se ha subido correctamente'" + uniqueFilename + "'");
			cliente.setFoto(uniqueFilename);

		}
		String mensajeFlash = (cliente.getId() != null) ? "Cliente modificado con exito!" : "Cliente creado con exito!";
		clienteService.save(cliente);
		status.setComplete();
		flash.addFlashAttribute("success", mensajeFlash);
		return "redirect:listar";
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(value = "/eliminar/{id}")
	public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		if (id > 0) {
			Cliente cliente = clienteService.findOne(id);

			clienteService.delete(id);
			flash.addFlashAttribute("success", "Cliente Eliminado");
			
			/*if(uploadFileService.delete(cliente.getFoto())) {
				flash.addFlashAttribute("info", "Foto " + cliente.getFoto() + " eliminada con exito!");
			}*/
			
		}
		return "redirect:/listar";
	} 
	
	private boolean hasRole(String role) {
		
		 SecurityContext context = SecurityContextHolder.getContext();
		 if (context == null) {
			 return false;
		 }
		 Authentication auth = context.getAuthentication();
		 if(auth == null) {
			 return false;
		 }
		 Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
		 for(GrantedAuthority authority: authorities) {
			 if(role.equals(authority.getAuthority())) {
				 logger.info("Holis".concat(auth.getName()).concat("tu role es: ".concat(authority.getAuthority())));
				 return true;
			 }
		 }
		 return false;
		 //return authorities.contains(new SimpleGrantedAuthority(role));
	}

}
