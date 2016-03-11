#!/usr/bin/python
# -*- coding: utf-8 -*-
import os

current_dir = os.path.dirname(os.path.realpath(__file__))

# set language for stemmer and stopwords
input_language = 'french'

# header #
category_3 = 'Categorie3'
category_2 = 'Categorie2'
category_1 = 'Categorie1'
# test and training files' header
ID_Product = 'Identifiant_Produit'
header_description = 'Description'
header_label = 'Libelle'
header_brand = 'Marque'
header_price = 'prix'
Produit_Cdiscount = 'Produit_Cdiscount'

# prices not between price_min and price_max will be set to price_default 
price_max = 100
price_min = 0
price_default = 0

# delimiter for csv files
csv_delimiter = ';'

count_line = 1000
# stopwords
path_stopword_1 = current_dir+'/stop-words_french_1_fr.txt'
path_stopword_2 = current_dir+'/stop-words_french_2_fr.txt'
# words that appear in almost product lines, that will not improve the classification algorithms 
additional_stopwords = ['voir', 'presentation']
stopwords = ['au', 'aux', 'avec',
             'ce', 'ces', 'dans', 'de', 'des',
             'du', 'elle', 'en', 'et', 'eux',
             'il', 'je', 'la', 'le', 'leur',
             'lui', 'ma', 'mais', 'me', 'même',
             'mes', 'moi', 'mon', 'ne', 'nos', 'notre',
             'nous', 'on', 'ou', 'par', 'pas', 'pour',
             'qu', 'que', 'qui', 'sa', 'se', 'ses',
             'son', 'sur', 'ta', 'te', 'tes', 'toi',
             'ton', 'tu', 'un', 'une', 'vos', 'votre',
             'vous', 'c', 'd', 'j', 'l', 'à', 'm', 'n',
             's', 't', 'y', 'été', 'étée', 'étées',
             'étés', 'étant', 'étante', 'étants',
             'étantes', 'suis', 'es', 'est', 'sommes',
             'êtes', 'sont', 'serai', 'seras', 'sera',
             'serons', 'serez', 'seront', 'serais', 'serait',
             'serions', 'seriez', 'seraient', 'étais', 'était',
             'étions', 'étiez', 'étaient', 'fus', 'fut',
             'fûmes', 'fûtes', 'furent', 'sois', 'soit',
             'soyons', 'soyez', 'soient', 'fusse', 'fusses',
             'fût', 'fussions', 'fussiez', 'fussent', 'ayant',
             'ayante', 'ayantes', 'ayants', 'eu', 'eue', 'eues',
             'eus', 'ai', 'as', 'avons', 'avez', 'ont', 'aurai',
             'auras', 'aura', 'aurons', 'aurez', 'auront',
             'aurais', 'aurait', 'aurions', 'auriez', 'auraient',
             'avais', 'avait', 'avions', 'aviez', 'avaient', 'eut',
             'eûmes', 'eûtes', 'eurent', 'aie', 'aies', 'ait',
             'ayons', 'ayez', 'aient', 'eusse', 'eusses', 'eût',
             'eussions', 'eussiez', 'eussent']
