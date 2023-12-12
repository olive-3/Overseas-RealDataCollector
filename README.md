### tr_key :	D거래소명종목코드
D+시장구분(3자리)+종목코드  
예) DNASAAPL : D+NAS(나스닥)+AAPL(애플)  
[시장구분]  
NYS : 뉴욕, NAS : 나스닥, AMS : 아멕스,  
TSE : 도쿄, HKS : 홍콩,  
SHS : 상해, SZS : 심천  
HSX : 호치민, HNX : 하노이  


## 빌드하고 실행하기
### MAC
1. ` ./gradlew build --exclude-task test`
2. `cd build/libs`
3. `java -jar overseas-0.0.1-SNAPSHOT.jar`
4. 실행 확인  

**주의!**  
"RealDataCollector.json 파일이 존재하지 않는다" 예외 발생 할 경우 /stock/build/libs 아래 RealDataCollector.json 파일 넣어줘야한다.

**참고**  
build 하다 에러 발생하는 경우 /stock 위치에서 `./gradlew clean` 실행하면 된다. 그러면 build 파일이 사라진다.  

**예시 이미지 첨부** 

### 윈도우
1. 콘솔로 이동 명령 프롬프트(cmd)로 이동
2. `gradlew build --exclude-task test`
3. `cd build/libs`
4. `java -jar overseas-0.0.1-SNAPSHOT.jar`
5. 실행 확인

