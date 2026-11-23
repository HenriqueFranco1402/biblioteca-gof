package br.pucpr.biblioteca.cenarios;

import br.pucpr.biblioteca.cadastro.CadastroDeEstudante;
import br.pucpr.biblioteca.cadastro.CadastroTemplate;
import br.pucpr.biblioteca.cadastro.Dados;
import br.pucpr.biblioteca.cadastro.PreRequisitoNaoAtendidoException;
import br.pucpr.biblioteca.cadastro.ResultadoCadastro;
import br.pucpr.biblioteca.dominio.pessoa.Estudante;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Requisito 7 - Rotina comum para cadastros (Template Method). */
@DisplayName("R07 - Template Method: rotina de cadastro")
class R07_RotinaDeCadastroTemplateMethodTest extends CenarioBase {

    @Test
    @DisplayName("R07 cenario: incluir estudante duplicado retorna o cadastro existente")
    void R07_estudanteDuplicadoRetornaCadastroExistente() {
        ResultadoCadastro r = sistema.tratadorDeEstudante().incluirEstudante(new Dados.Estudante("E1", "11144477735"));

        assertFalse(r.novo(), "nao cria de novo");
        assertEquals("E1", r.id());
        assertTrue(r.descricao().contains("Ana Souza"));
    }

    @Test
    @DisplayName("R07 cenario: novo estudante com CPF nao cadastrado falha antes de qualquer gravacao")
    void R07_estudanteComCpfNaoCadastradoFalhaAntesDeGravar() {
        List<String> passos = new ArrayList<>();
        // subclasse espia: registra os passos chamados pelo metodo-modelo (que e final)
        CadastroDeEstudante espia = new CadastroDeEstudante(sistema.fabrica().unidadeDeTrabalho(), sistema.relacionamento()) {
            @Override
            protected void validar(Dados.Estudante d) { passos.add("validar"); super.validar(d); }

            @Override
            protected void verificarPreRequisitos(Dados.Estudante d) { passos.add("preRequisitos"); super.verificarPreRequisitos(d); }

            @Override
            protected Estudante criar(Dados.Estudante d) { passos.add("criar"); return super.criar(d); }

            @Override
            protected void armazenar(Estudante e) { passos.add("armazenar"); super.armazenar(e); }
        };

        assertThrows(PreRequisitoNaoAtendidoException.class,
                () -> espia.incluir(new Dados.Estudante("E9", "987.654.321-00")));

        assertEquals(List.of("validar", "preRequisitos"), passos, "parou no pre-requisito: nao criou nem armazenou");
        assertTrue(sistema.relacionamento().consultarEstudante("E9").isEmpty(), "nenhuma gravacao");
    }

    @Test
    @DisplayName("R07: o metodo-modelo incluir e final e fixa a ordem dos passos")
    void R07_metodoModeloEFinalEOrdemFixa() throws Exception {
        assertTrue(Modifier.isFinal(CadastroTemplate.class.getMethod("incluir", Object.class).getModifiers()));

        List<String> passos = new ArrayList<>();
        sistema.tratadorDePessoa().incluirPessoa(new Dados.Pessoa("987.654.321-00", "Davi", "Rocha", "davi@pucpr.br"));
        CadastroDeEstudante espia = new CadastroDeEstudante(sistema.fabrica().unidadeDeTrabalho(), sistema.relacionamento()) {
            @Override
            protected java.util.Optional<Estudante> buscarExistente(Dados.Estudante d) { passos.add("buscar"); return super.buscarExistente(d); }

            @Override
            protected void validar(Dados.Estudante d) { passos.add("validar"); }

            @Override
            protected void verificarPreRequisitos(Dados.Estudante d) { passos.add("preRequisitos"); super.verificarPreRequisitos(d); }

            @Override
            protected Estudante criar(Dados.Estudante d) { passos.add("criar"); return super.criar(d); }

            @Override
            protected void armazenar(Estudante e) { passos.add("armazenar"); super.armazenar(e); }

            @Override
            protected ResultadoCadastro apresentar(Estudante e, boolean novo) { passos.add("apresentar"); return super.apresentar(e, novo); }
        };

        ResultadoCadastro r = espia.incluir(new Dados.Estudante("E9", "98765432100"));

        assertTrue(r.novo());
        assertEquals(List.of("buscar", "validar", "preRequisitos", "criar", "armazenar", "apresentar"), passos);
        assertTrue(sistema.relacionamento().consultarEstudante("E9").isPresent());
    }

    @Test
    @DisplayName("R07: Livro, Pessoa e Atendente usam a mesma rotina")
    void R07_outrosCadastrosUsamAMesmaRotina() {
        assertFalse(sistema.tratadorDeAcervo().incluirLivro(new Dados.Livro("L1", "Outro titulo", null)).novo());
        assertTrue(sistema.tratadorDeAcervo().incluirLivro(new Dados.Livro("L9", "Novo", "X")).novo());
        assertFalse(sistema.tratadorDePessoa().incluirPessoa(new Dados.Pessoa("11144477735", "Ana", "Souza", null)).novo());
        assertThrows(PreRequisitoNaoAtendidoException.class,
                () -> sistema.tratadorDeAtendente().incluirAtendente(new Dados.Atendente("A9", "12312312312")));
        assertTrue(sistema.relacionamento().consultarAtendente("A9").isEmpty());
    }
}
