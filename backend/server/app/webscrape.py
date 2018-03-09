import bs4 as bs
import urllib.request

urls = {
    "deti" : "http://www.ua.pt/deti/PageText.aspx?id=619",
    "ieeta" : "http://www.ua.pt/deti/pagetext.aspx?id=575",
    "biblioteca" : "https://www.ua.pt/sbidm/biblioteca/page/14470"
}


def process_search(query):
    if(query == 'biblioteca'):
        return search_bibs(query)
    else:
        return search_department(query)

def search_bibs(query):
    link = urls[query]
    sauce = urllib.request.urlopen(link).read()
    soup = bs.BeautifulSoup(sauce, 'lxml')
    block = soup.find('blockquote')

    for child in block.find_all('p'):
        child.decompose()

    #print(block.text.strip())
    return block.text.strip()

def search_department(query):
    link = urls[query]
    sauce = urllib.request.urlopen(link).read()
    soup = bs.BeautifulSoup(sauce, 'lxml')
    summary = soup.find('p')
    #print(summary.text)
    return summary.text

