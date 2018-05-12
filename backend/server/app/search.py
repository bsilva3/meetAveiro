'''
Contém métodos para pesquisar informação na wikipedia usando
a wikimedia API

search_wiki(search_field, lang) -> recebe o conceito a pesquisar e
a linguagem da wikipedia a usar e retorna o resumo da página encontrada
'''

import wikipedia

def search_wiki(search_field, lang='PT'):
    '''
    Usa a API da wikimedia para fazer pesquisas na wikipedia
    e retorna o resumo do conceito pesquisado

    search-field - string com o termo a pesquisar
    
    lang - lingua da wikipedia (default: "PT")
    '''
    wikipedia.set_lang(lang) # mudar a linguagem para português

    try:
        result = wikipedia.page(search_field) # tentar procurar página correspondente
        #print(result.summary) # mostrar apenas a info resumida
        return result.summary
    except wikipedia.exceptions.DisambiguationError as e: # ocorre quando o objeto de pesquisa é ambiguo
        print(e.options) # mostra as diferentes sugestões
    except wikipedia.exceptions.PageError as e: # ocorre quando não é encontrado nenhum resultado
        print("Página não encontrada")
        print(wikipedia.search(search_field, 5)) # mostra sugestões (se existirem)

    

