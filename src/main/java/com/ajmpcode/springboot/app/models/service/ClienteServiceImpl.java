package com.ajmpcode.springboot.app.models.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ajmpcode.springboot.app.models.dao.IClienteDao;
import com.ajmpcode.springboot.app.models.dao.IFacturaDao;
import com.ajmpcode.springboot.app.models.dao.IProductoDao;
import com.ajmpcode.springboot.app.models.entity.Cliente;
import com.ajmpcode.springboot.app.models.entity.Factura;
import com.ajmpcode.springboot.app.models.entity.Producto;

@Service
public class ClienteServiceImpl implements IClienteService {

	@Autowired
	private IClienteDao clienteDao;

	@Autowired
	private IProductoDao productoDao;

	@Autowired
	private IFacturaDao facturaDao;

	@Override
	public List<Cliente> findAll() {
		return (List<Cliente>) clienteDao.findAll();
	}

	@Override
	@Transactional
	public void save(Cliente cliente) {
		clienteDao.save(cliente);
	}

	@Override
	public Cliente findOne(Long id) {
		return clienteDao.findById(id).orElse(null);
	}

	@Override
	public Cliente fetchByIdWithFacturas(Long id) {
		return clienteDao.fetchByIdWithFacturas(id);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		clienteDao.deleteById(id);
	}

	@Override
	public Page<Cliente> findAll(Pageable pageable) {
		return clienteDao.findAll(pageable);
	}

	@Override
	@Transactional
	public List<Producto> findByNombre(String term) {
		return productoDao.findByNombreLikeIgnoreCase("%" + term + "%");
	}

	@Override
	@Transactional
	public void saveFactura(Factura factura) {
		facturaDao.save(factura);
	}

	@Override
	@Transactional
	public Producto findProductoById(Long id) {
		return productoDao.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public Factura findFacturaById(Long id) {
		return facturaDao.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public void deleteFactura(Long id) {
		facturaDao.deleteById(id);
	}

	@Override
	@Transactional
	public Factura fetchFacturaByIdWithClienteWithItemFacturaWithProducto(Long id) {
		return facturaDao.fetchByIdWithClienteWithItemFacturaWithProducto(id);
	}

}
