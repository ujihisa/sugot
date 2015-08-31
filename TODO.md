* protocolを継承するprotocolどうやって定義する?
    * scalaのtraitのself-type constraintみたいなやつ
    * これ未解決なので、sugot.mocksでgetPlayerとかの名前がかぶってると怒られる
    * かといってreifyする側で全部指定するのもよくない
* Player系eventのpの有無の混在
    * いまはcore.cljでtry/catchで力技で解決してる
