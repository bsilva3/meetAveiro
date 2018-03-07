''' from search import search_wiki 
import webscrape


def test():
    print("Your query: ")
    query = input()
    if query in webscrape.urls.keys():
        webscrape.process_search(query)
    else:
        if query == 'reitoria':
            query = 'Universidade de Aveiro'
        search_wiki(query)

test() '''
