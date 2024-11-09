# Druga laboratorijska vježba — Praćenje senzorskih očitanja u vremenu

Ovaj repozitorij sadrži izvorni kôd za projekt raspodijeljenog sustava senzorske mreže, razvijen kao dio laboratorijske vježbe iz kolegija Raspodijeljeni sustavi na FER3, akademska godina 2024./2025.

## Pregled
Cilj projekta je implementirati decentralizirani sustav senzora s ravnopravnim čvorovima, koristeći Kafka za koordinaciju i UDP za komunikaciju između senzora. Ključni ciljevi uključuju izgradnju mreže raspodijeljenih čvorova, obradu očitanja senzora u stvarnom vremenu te primjenu mehanizama za sinkronizaciju.

## Značajke
Komunikacija među čvorovima: Svaki čvor djeluje kao senzor koji dijeli očitanja s ostalima putem UDP-a.
Centralizirana koordinacija: Upravljački čvor (Kafka) upravlja registracijom i radom senzorskih čvorova.
Sinkronizacija podataka: Čvorovi razmjenjuju očitanja i sinkroniziraju se pomoću skalarnog i vektorskog označavanja vremena.
Otpornost podataka: Implementirana je retransmisija izgubljenih UDP paketa kako bi se osigurala dostupnost podataka.

## Arhitektura sustava
- Čvorovi senzora povezani su u potpunu mrežu i komuniciraju putem UDP-a.
- Kontrolni čvor (Kafka) šalje kontrolne poruke (npr. "Start" i "Stop") za pokretanje i zaustavljanje rada senzorskih čvorova.
- UDP Komunikacija koristi simulaciju gubitka paketa, uz postotak gubitka postavljen na 30% i kašnjenje od 1000 ms.

## Autor
- Iva Svalina
- Fakultet elektrotehnike i računarstva, Sveučilište u Zagrebu
