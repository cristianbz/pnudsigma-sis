/**
@autor proamazonia [Christian Báez]  17 may. 2022

**/
package ec.gob.ambiente.sigma.ejb.dto.sis;

import lombok.Getter;
import lombok.Setter;

public class DtoDatosProyectoResumen {
	@Getter
	@Setter
	private String proyecto;
	@Getter
	@Setter
	private String partner;
	@Getter
	@Setter
	private String sector;
}

