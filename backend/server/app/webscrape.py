'''
Contém métodos que usam web crawlers para pesquisar informação na internet

process_search(query) -> recebe uma query a pesquisar e recolhe informação da wikipedia
sobre a mesma (caso a wikipedia contenha uma entrada válida sobre ela)
'''

import bs4 as bs
import urllib.request
import json

# urls para os edificios da universidade
urls = {
    "deti" : "http://www.ua.pt/deti/PageText.aspx?id=619",
    "ieeta" : "http://www.ua.pt/deti/pagetext.aspx?id=575",
    "biblioteca" : "https://www.ua.pt/sbidm/biblioteca/page/14470"
}

eventos = "https://www.viralagenda.com/pt/aveiro"

def process_search(query):
    '''
    Usa um web crawler para retirar informações do site da UA 
    (usado para os edificios da universidade)

    query - string com o nome do departamento
    '''
    if(query == 'biblioteca'):
        return search_bibs(query)
    else:
        return search_department(query)


def search_bibs(query):
    '''
    Processa a página web sobre as bibliotecas da UA

    query - biblioteca a especificar (ex: "biblioteca" -> Biblioteca de Santiago)
    '''
    link = urls[query]
    sauce = urllib.request.urlopen(link).read()
    soup = bs.BeautifulSoup(sauce, 'html5lib')
    block = soup.find('blockquote')

    for child in block.find_all('p'):
        child.decompose()

    #print(block.text.strip())
    return block.text.strip()

def search_department(query):
    '''
    Extrai informação sobre o departamento especificado no site da UA

    query - nome do departamento (ex: deti, ieeta)
    '''
    link = urls[query]
    sauce = urllib.request.urlopen(link).read()
    soup = bs.BeautifulSoup(sauce, 'html5lib')
    summary = soup.find('p')
    #print(summary.text)
    return summary.text.strip()


def search_eventos():
    '''
    Extrai informação sobre eventos anunciados no site viralagenda

    (em desenvolvimento)
    '''
    sauce = urllib.request.urlopen(eventos).read()
    soup = bs.BeautifulSoup(sauce, 'html5lib')

    events = soup.findAll('div', {"class": "viral-event-title"})
    #hour = soup.findAll('div', {"class": "viral-event-hour"})
    #place = soup.findAll('a', {"class": "viral-event-place"})
    #eventType = soup.findAll('div', {"class": "viral-event-box viral-event-box-other viral-event-box-cat"})
    
    for i in range(0, len(events)):
        base_url = 'https://www.viralagenda.com/'
        anchor = events[i].find('a')
        #opener = urllib.request.build_opener()
        #opener.addheaders = [('User-agent', 'Mozilla/5.0')]
        #data = opener.open(base_url + anchor['href']).read()
        temp_sauce = urllib.request.urlopen(base_url + anchor['href']).read()
        temp_soup = bs.BeautifulSoup(temp_sauce, 'html5lib')
        name = temp_soup.find('h1')
        #hour = temp_soup.find('div', {'class': 'time'})
        #time = hour.find('span', {'class': 'viral-event-slot'})

        

        print(name.text.strip())
        #print(time.text.strip())
        print()

        #print(name[i].text.strip())
        #print(hour[i].text.strip())
        #print(place[i].text.strip())
        #print(eventType[i].text.strip())
        #print()


def search_turismo():
    '''
    Extrai informação sobre eventos descritos no site do turismo de Aveiro
    e devolve-os numa lista de dicionarios
    '''
    turismo_url = 'https://turismoinaveiro.com/collections/experiencias-cidade-de-aveiro'
    sauce = urllib.request.urlopen(turismo_url).read()
    soup = bs.BeautifulSoup(sauce, 'html5lib')
    events = soup.find_all('a', {'class': 'product-card'})

    result = []

    for e in events:
        res = {}
        temp_url = 'https://turismoinaveiro.com/' + e['href']
        temp_sauce = urllib.request.urlopen(temp_url).read()
        temp_soup = bs.BeautifulSoup(temp_sauce, 'html5lib')
        title = temp_soup.find('h1')
        price = temp_soup.find('span', {'class': 'product-single__price'})
        table = temp_soup.find('table')
        if table is not None:
            tr = table.find('tr')
            td = tr.find('td')
            ps = td.find_all('p')
        #print(title.text.strip())
        #print(price.text.strip())
        res['title'] = title.text.strip()
        preco = price.text.strip()
        res['price'] = float(preco.replace('€', ''))

        if len(ps) == 3:
            
            res['location'] = ps[1].text.strip()
            coordinates = ps[2].text.strip()
            i = 0
            coordinates = coordinates[17:].split(' ')
            for c in coordinates:
                coords = []
                temp = c.split('°')
                coords.append(float(temp[0]))
                #print(temp[0])
                temp = temp[1].split('\'')
                coords.append(float(temp[0])/60.0)
                #print(temp[0])
                seconds = temp[1].replace('W', '')
                seconds = seconds.replace('N', '')
                seconds = seconds.replace('"', '')
                coords.append(float(seconds)/3600.0)
                #print(seconds)
                if i == 0:
                    res['latitude'] = sum(coords)
                    #print(sum(coords))
                else:
                    res['longitude'] = 0 - sum(coords)
                    #print(sum(coords))
                i+=1
        else:
            res['location'] = ps[1].text.strip()
        result.append(res)
        #break
    with open('./static/results/events.txt', 'w', encoding='utf8') as json_file:
        json.dump(result, json_file, ensure_ascii=False)

#search_eventos()
search_turismo()
