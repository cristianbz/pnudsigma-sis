package ec.gob.ambiente.sigma.ejb.entidades.sis;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.Table;

import ec.gob.ambiente.sigma.ejb.entidades.Project;
import ec.gob.ambiente.sigma.ejb.entidades.ProjectsStrategicPartner;
import ec.gob.ambiente.sigma.ejb.entidades.Safeguard;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity implementation class for Entity: ProjectQuestions
 *
 */
@Entity
@Table(name = "project_questions", schema = "sis")
@NamedQueries({
//	@NamedQuery(name = "q1",query = "SELECT PQ FROM ProjectQuestions PQ WHERE PQ.projectsStrategicPartners.pspaId =1 AND PQ.prquStatus=TRUE")	
})
public class ProjectQuestions implements Serializable {

	
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "prqu_id")
    private Integer prquId;
	
	@Getter
	@Setter
	@Column(name = "prqu_status")
	private boolean prquStatus;
	
	   
	@Getter
	@Setter
	@JoinColumn(name = "cata_id")
	@ManyToOne(fetch = FetchType.EAGER)	
	private Catalogs catalogs;
	
	@Getter
	@Setter
	@JoinColumn(name = "pspa_id")
	@ManyToOne(fetch = FetchType.EAGER)	
	private ProjectsStrategicPartner projectsStrategicPartners;
	
	@Getter
	@Setter
	@JoinColumn(name = "proj_id")
	@ManyToOne(fetch = FetchType.EAGER)	
	private Project projects;
	
	@Getter
	@Setter
	@JoinColumn(name = "safe_id")
	@ManyToOne(fetch = FetchType.EAGER)	
	private Safeguard safeguards;
}
