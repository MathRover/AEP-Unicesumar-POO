package aep.persistencia;

import java.util.List;
import java.util.Optional;

public interface Repositorio<K, V> {
    Optional<V> buscar(K chave);
    List<V> listar();
    void salvar(K chave, V valor);
    void remover(K chave);
}
