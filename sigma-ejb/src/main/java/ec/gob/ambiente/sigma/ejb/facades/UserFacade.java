package ec.gob.ambiente.sigma.ejb.facades;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import ec.gob.ambiente.sigma.ejb.dao.AbstractFacade;
import ec.gob.ambiente.sigma.ejb.entidades.Organization;
import ec.gob.ambiente.sigma.ejb.entidades.People;
import ec.gob.ambiente.sigma.ejb.entidades.User;
import ec.gob.ambiente.sigma.ejb.exceptions.DaoException;
import ec.gob.ambiente.sigma.ejb.exceptions.ServiceException;
import ec.gob.ambiente.sigma.ejb.services.RoleFacade;
import ec.gob.ambiente.sigma.ejb.services.RolesUserFacade;


@Stateless
public class UserFacade extends AbstractFacade<User, Integer> implements Serializable {
	
	private static final long serialVersionUID = -4594424897085245484L;


	public UserFacade() {
		super(User.class, Integer.class);		
	}
	
	
	
	@EJB
	private RolesUserFacade rolesUserFacade;
	
	@EJB
	private RoleFacade roleFacade;
	
	@EJB
	private OrganizationFacade organizationFacade;

	@EJB
	private PeopleFacade peopleFacade;

	/*@EJB
	private GeographicalLocationFacade geographicalLocationFacade;
	
	@EJB
	private ContactFacade contactFacade;*/

	@SuppressWarnings("unchecked")
	public User getUserById(Integer userId)
	{
		//return userfacade.find(userId);
		try {
			Query query = getEntityManager().createQuery(" SELECT u FROM User u where u.userId= :userId");		
			query.setParameter("userId",	userId);
			return (User) query.getSingleResult();			
		} catch (NoResultException e) {
			return null;
		}
	}
	/*
	public void save(User user, NotificacionesMails notificacionesMail, boolean usuarioExterno, String rolProponente) throws ServiceException {
		
		//user.getPeople().setPeopPin(user.getUserName());
        
        if(user.getUserId() != null)
        {
        	edit(user);
        }
        else
        {
        	create(user);
        }        
              
        
        if (usuarioExterno) {            
            rolesUserFacade.save(user,roleFacade.findByName(rolProponente));
        }
    }*/
	
	/**
	 * Actualizar datos de perfil de usuario
	 * @param user
	 */
	/*public void updateUserData(User user,Organization organization) {

		//Actualizar Usuario
		//Se actualiza en cascada la tabla personas
		edit(user);
		
		//Actualizar, crear o remover contactos
		//ContactService contactService=new ContactService();
		for (Contact contact : user.getPeople().getContacts()) {
			if(contact.getContId()==null)				
				contactFacade.create(contact);							
			else
				contactFacade.edit(contact);
		}
		
		if(organization!=null&&organization.getOrgaId()!=null)
			organizationFacade.edit(organization);

		
		
	}
	
	public Organization getOrganizationByPeopleAndRuc(People people,String orgaRuc)
	{
		return organizationFacade.findByPeopleAndRuc(people,orgaRuc);
	}
	
	public People getPeopleByPin(String peopPin) {
	
		return peopleFacade.findByPin(peopPin);
	}*/
	
	
	
	
	public String getCompleteNameByUserName(String userName)
	{
		Organization orga=organizationFacade.findByRuc(userName);
		if(orga!=null&&orga.getOrgaNameOrganization()!=null)
			return orga.getOrgaNameOrganization();
		
		TypedQuery<User> query = super.getEntityManager().createQuery("select u from User u where u.userName = :userName", User.class);
		
		query.setParameter("userName", userName);
		
		String completeName = "";
		if(query.getResultList().size() > 0)
		{
			completeName = query.getResultList().get(0).getPeopId().getPeopName();
		}
		
		return completeName;
	}
	
	public User findByUserName(String userName)
	{
		TypedQuery<User> query = super.getEntityManager().createQuery("select u from User u "
				+ "where u.userName = :userName and u.userStatus = true", User.class);
		
		query.setParameter("userName", userName);
		
		User user = new User();
		
		if(query.getResultList().size() > 0)
		{
			user = query.getResultList().get(0);
		}
		
		return user;
	}
	
	public User findByUserNameDisabled(String userName)
	{
		TypedQuery<User> query = super.getEntityManager().createQuery("select u from User u "
				+ "where u.userName = :userName and u.userStatus = false", User.class);
		
		query.setParameter("userName", userName);
		
		User user = new User();
		
		if(query.getResultList().size() > 0)
		{
			user = query.getResultList().get(0);
		}
		
		return user;
	}
	
	public List<User> find_all(){		
		return findAll();
	}
	
	public User updateUser(User usuario){		
		return edit(usuario);
	}
	
	public User findByPeople(People persona) {
		User result = null;
		
		Query q = getEntityManager().createQuery("SELECT u FROM User u WHERE u.people=:persona");
		q.setParameter("persona", persona);
		
		if(q.getResultList().size() > 0) {
			result = (User)q.getResultList().get(0);
		}
		
		return result;
	}
	
	public List<User> listUserReasignation() throws ServiceException {
		List<User> result = new ArrayList<User>();
		Query q = getEntityManager().createNativeQuery("select u.user_id, u.user_name, u.user_creation_date, r.role_name, p.peop_name, u.user_status "
				+"from users AS u LEFT OUTER JOIN suia_iii.roles_users AS ru ON u.user_id = ru.user_id "
				+"LEFT OUTER JOIN suia_iii.roles AS r ON r.role_id = ru.role_id "
				+"left join people p ON u.peop_id = p. peop_id "
				+"LEFT OUTER JOIN areas as ar ON ar.area_id=u.area_id "		
				+"where r.role_name in ('Consultores-Técnico Prevención','Consultores-Técnico Jurídico','Consultores-Coordinador Jurídico','Consultores-Director Prevención','Consultores-Técnico Control','Consultores-Director Control','Consultores-Subsecretario Calidad Ambiental') and u.user_status=true order by u.user_id");
		if(q.getResultList().size() > 0) {
			List<Object[]>  resultList = (List<Object[]>)q.getResultList();
			for (Object object : resultList) {
				User usuarioVO = new User();
				People opeople = new People();
				Object[] dataUserVo = (Object[]) object;
				usuarioVO.setUserId((Integer) dataUserVo[0]);
				usuarioVO.setUserName((String) dataUserVo[1]);
				opeople.setPeopName((String) dataUserVo[4]);
				usuarioVO.setPeopId(opeople);
				result.add(usuarioVO);
			}
		}
		return result;
	}
	
	
    public User obtenerUsuarioConPassword(String userName, String claveSHA1) throws Exception{
    	String sql="select u from User u where UPPER(u.userName)=:param1 and UPPER(u.userPassword)=:param2 and u.userStatus=:param3"; 
    	Map<String, Object> camposCondicion = new HashMap<String, Object>();
         camposCondicion.put("param1", userName.toUpperCase());
         camposCondicion.put("param2", claveSHA1.toUpperCase());
         camposCondicion.put("param3", true);
         List<User> res = findByCreateQuery(sql,camposCondicion);
         if (res != null && res.size() > 0) {
             return res.get(0);
         } else {
             return null;
         }
    }
    /**
	 * Validacion del usuario se envia como parametro un objeto usuario
	 * @param usuario
	 * @return
	 * @throws DaoException
	 */
	public User validarUsuario(String usuario,String clave)throws DaoException{
		try{
			String sql="SELECT U FROM User U WHERE U.userName=:usuario AND U.userPassword=:clave AND U.userStatus=TRUE";
			Map<String, Object> camposCondicion=new HashMap<String, Object>();
			camposCondicion.put("usuario", usuario);
			camposCondicion.put("clave", clave);
			return findByCreateQuerySingleResult(sql, camposCondicion);
		}catch(NoResultException e){
			return null;
		}catch(Exception e){
			throw new DaoException();
		}

	}
	
	public List<User> listaUsuariosFiltrados(String filtro)throws Exception{
		String sql="SELECT U FROM User U WHERE U.userName LIKE :filtro AND U.userStatus=TRUE";
		Map<String, Object> camposCondicion=new HashMap<String, Object>();
		camposCondicion.put("filtro","%"+filtro+"%");		
		return findByCreateQuery(sql, camposCondicion);
	}
	
	public List<User> findByRolEstrategicoImplementador(String filtro) throws Exception{
		String sql = "SELECT u FROM RolesUser o,Users u WHERE  o.users.userId=u.userId AND o.role.roleName IN('SIS_socio_implementador','SIS_socio_estrategico') AND o.rousStatus = TRUE AND o.users.userName LIKE :filtro ";
		Map<String, Object> camposCondicion=new HashMap<String, Object>();
		camposCondicion.put("filtro","%"+filtro+"%");
		return findByCreateQuery(sql, camposCondicion);	
	}
	
}