package ufc.ceub.organizacao_cursos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração da documentação OpenAPI (Swagger) da aplicação.
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI organizacaoCursosOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Organização de Cursos API")
						.version("1.0")
						.description("API para gestão de professores, cursos e matrículas de alunos"));
	}

}
