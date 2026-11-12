package aep.persistencia;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class RepositorioEmMemoria<K, V> implements Repositorio<K, V> {
    private final Map<K, V> registros = new LinkedHashMap<>();

    @Override
    public Optional<V> buscar(K chave) { return Optional.ofNullable(registros.get(chave)); }

    @Override
    public List<V> listar() { return List.copyOf(registros.values()); }

    @Override
    public void salvar(K chave, V valor) { registros.put(chave, valor); }

    @Override
    public void remover(K chave) { registros.remove(chave); }
}
