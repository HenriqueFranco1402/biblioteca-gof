package br.pucpr.biblioteca.dominio.livro;

import br.pucpr.biblioteca.dominio.RegraDeNegocioException;
import br.pucpr.biblioteca.dominio.movimentacao.Emprestimo;

import java.time.LocalDate;
import java.util.Objects;

/**
 * PADRAO STATE - papel: Contexto.
 *
 * <p>O Livro nao decide nada sobre o seu ciclo de vida: cada evento (reservar, emprestar,
 * renovar...) e simplesmente delegado ao objeto de estado atual. Nao ha nenhum {@code if}
 * ou {@code switch} sobre a situacao do livro aqui nem nos tratadores.</p>
 *
 * <p>Persistencia: o estado e gravado pelo seu nome ({@link #getNomeEstado()}, ex.: "EMPRESTADO")
 * e reconstituido por {@link #reconstituir}, que usa {@link EstadoLivro#deNome(String)}.</p>
 */
public class Livro {

    /** Limite da guarda numRenovacoes &lt; 2 (premissa da lista de problemas). */
    public static final int LIMITE_RENOVACOES = 2;

    private final String idLivro;
    private String titulo;
    private String autor;
    private EstadoLivro estado;
    private int numRenovacoes;
    private LocalDate dataUltimaDevolucao;

    private Livro(String idLivro, String titulo, String autor, EstadoLivro estado,
                  int numRenovacoes, LocalDate dataUltimaDevolucao) {
        if (idLivro == null || idLivro.isBlank()) {
            throw new RegraDeNegocioException("Livro precisa de identificador");
        }
        this.idLivro = idLivro;
        this.titulo = titulo;
        this.autor = autor;
        this.estado = Objects.requireNonNull(estado);
        this.numRenovacoes = numRenovacoes;
        this.dataUltimaDevolucao = dataUltimaDevolucao;
    }

    /** Livro recem-cadastrado: o estado inicial e LIVRE (pseudoestado inicial do diagrama). */
    public static Livro novo(String idLivro, String titulo, String autor) {
        return new Livro(idLivro, titulo, autor, EstadoLivre.INSTANCIA, 0, null);
    }

    /** Reconstitui um livro lido da persistencia (o estado vem gravado como texto). */
    public static Livro reconstituir(String idLivro, String titulo, String autor, String nomeEstado,
                                     int numRenovacoes, LocalDate dataUltimaDevolucao) {
        return new Livro(idLivro, titulo, autor, EstadoLivro.deNome(nomeEstado), numRenovacoes, dataUltimaDevolucao);
    }

    // ---------------- eventos da maquina de estados (todos delegados ao estado atual) ----------------

    public void reservar() { estado.reservar(this); }

    public void emprestar() { estado.emprestar(this); }

    public void renovar(Emprestimo emprestimo) { estado.renovar(this, emprestimo); }

    public void devolver(LocalDate dataDevolucao) { estado.devolver(this, dataDevolucao); }

    public void aprovarInspecao() { estado.aprovarInspecao(this); }

    public void reprovarInspecao() { estado.reprovarInspecao(this); }

    public void enviarParaReparo() { estado.enviarParaReparo(this); }

    public void concluirReparo() { estado.concluirReparo(this); }

    public void registrarLaudoIrrecuperavel() { estado.registrarLaudoIrrecuperavel(this); }

    // ---------------- usados apenas pelos objetos de estado (visibilidade de pacote) ----------------

    void mudarEstado(EstadoLivro novo) {
        this.estado = novo;
        novo.aoEntrar(this);
    }

    void incrementarRenovacoes() { this.numRenovacoes++; }

    void zerarRenovacoes() { this.numRenovacoes = 0; }

    void registrarDataDevolucao(LocalDate data) { this.dataUltimaDevolucao = data; }

    // ---------------- consultas ----------------

    public String getIdLivro() { return idLivro; }

    public String getTitulo() { return titulo; }

    public String getAutor() { return autor; }

    public String getNomeEstado() { return estado.nome(); }

    public int getNumRenovacoes() { return numRenovacoes; }

    public LocalDate getDataUltimaDevolucao() { return dataUltimaDevolucao; }

    @Override
    public String toString() {
        return idLivro + " - " + titulo + " [" + estado.nome() + "]";
    }
}
