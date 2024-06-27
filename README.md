<div align="center">
<h1>📋 No Boilerplate 👨‍💻</h1>
단위 테스트 작성 시 발생하는 코드 중복을 제거하는 프로젝트
</div>

# Table of Contents

<details><summary><a href="#introduction">개요</a></summary></details>
<details><summary><a href="#main-feature">주요 구현</a></summary></details>
<details><summary><a href="#project-structure">프로젝트 구조</a></summary></details>
<details><summary><a href="#how-to-use">사용 가이드 </a><small> [접기/펼치기]</small></summary>

- [예제](#example-articleservice)
- [Mocker](#mocker)
- [Asserter](#asserter)
- [Context](#context)
- [TestTemplate](#testtemplate)
- [적용 결과](#적용-결과)

</details>
<details><summary><a href="#implementation-details">구현 상세 </a><small> [접기/펼치기]</small> </summary>

- [Mocker](#mocker-1)
- [Asserter](#asserter-1)
- [Context](#context-1)
- [TestTemplate](#testtemplate-1)

</details>
<details><summary><a href="#related-posts">관련 포스팅</a></summary></details>

# Introduction
- 개발 인원: 1명
- 개발 기간: 1개월(2024.03-2024.04)
- 기술 스택: Java 17, Spring Boot 3.2.2, JUnit 5, Mockito, AssertJ

# Main Feature
- 단위 테스트에서 수행할 모킹, 검증 작업을 람다식으로 간결하게 정의하고, 집약적으로 작성할 수 있습니다.
- JUnit 5의 [Parameterized Test](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parameterized-tests)를 사용할 수 있게 구현해, 테스트 대상 메소드에 대한 다양한 시나리오를 한 번에 검증할 수 있습니다.
- 함수형 인터페이스와 제네릭으로 모킹, 검증, 테스트 수행에 공통적으로 필요한 작업을 추상화해, 코드 중복을 최소화할 수 있습니다.

<div align="center">
<br>
<b>
⇒ 테스트 코드 작성량 평균 40% 감소(컨트롤러 테스트: 359줄 → 182줄, 서비스 테스트: 315줄 → 225줄)<br>
⇒ 신속하고 효율적인 테스트 작성, 수정이 가능해져, TDD를 보다 효과적으로 적용할 수 있게 됨<br><br>
</b>

[적용 결과 보기](#적용-결과)
</div>


# Project Structure
```
main/java/com/boldfaced7/noboilerplate
├── Asserter.java
├── Context.java
├── Mocker.java
└── TestTemplate.java
```

# How to Use
본 프로젝트의 목적은 다음과 같습니다.
- 단위 테스트에서 수행하는 모킹과 검증 작업을 정적 팩토리 메소드에 간결하게 정의하고, 집약적으로 작성
- 정적 팩토리 메소드에서 전달받은 값들을 바탕으로 테스트 대상 메소드에 대한 다양한 시나리오를 한 번에 검증

정적 팩토리 메소드에서 간결하게 모킹과 검증 작업을 정의하기 위해, Mocker와 Asserter 클래스를 작성하고, 두 클래스의 간편한 사용을 위해 Context 클래스를 도입했습니다.

본 가이드는 Mockito(BDDMockito), JUnit 5(특히 @ParameterizedTest), AssertJ의 사용을 전제로 작성되었습니다,

## Example: ArticleService
ArticleService의 getArticle()을 테스트하는 메소드를 작성하며 사용법을 알아보겠습니다.

### ArticleService
ArticleService는 articleRepository, articleCommentRepository, attachmentRepository, fileStore에 의존하고 있습니다.

```java
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleCommentRepository articleCommentRepository;
    private final AttachmentRepository attachmentRepository;
    private final FileStore fileStore;

    public ArticleDto getArticle(ArticleDto dto) {
        Article article = articleRepository.findById(articleId).orElseThrow(ArticleNotFoundException::new);        
        Page<ArticleComment> articleComments = articleCommentRepository.findAllByArticle(article, dto.getPageable());
        List<Attachment> attachments = attachmentRepository.findAllByArticle(article);
        List<String> attachmentUrls = fileStore.getUrls(attachments);
        // ...
    }
}
```

### ArticleServiceTest
Junit과 Mockito(BDDMockito)를 사용하기 때문에, Junit과 Mockito를 사용해 단위 테스트를 작성할 때 필요한 어노테이션을 모두 사용합니다.
```java
import static org.assertj.core.api.Assertions;
import static org.mockito.BDDMockito.*;

@DisplayName("ArticleService 테스트")
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @InjectMocks ArticleService articleService;
    @Mock ArticleRepository mockedArticleRepository;
    @Mock ArticleCommentRepository mockedArticleCommentRepository;
    @Mock AttachmentRepository mockedAttachmentRepository;
    @Mock FileStore mockedFileStore;
}
```

## Mocker
단위 테스트에서는 외부 의존성 제거를 위해 검증 대상 객체가 의존하는 객체를 가짜로 만들고, 모킹 객체 메소드 호출 시 수행할 행동을 미리 정해둡니다.
Mocker는 정적 팩토리 메소드에서 간결하게 모킹 작업을 정의하고, 이를 테스트 메소드에서 실행하는 것을 돕습니다.

<div align="center">
<details>
<summary><b>Field</b></summary>
<div align="left">

```java
import static org.mockito.BDDMockito.*;

public class Mocker<T> {
    private final List<Consumer<T>> givens = new ArrayList<>();
    private final List<Consumer<T>> thens = new ArrayList<>();
    private String message;
    // ...
}
```
- T: 타입  매개변수에는 모킹 인스턴스에 접근할 수 있는 클래스의 타입을 전달
- givens: Mockito의 when(), BDDMockito의 given() 메소드를 통한 작업을 수행할 Consumer를 저장
- thens: Mockito의 verify(), BDDMockito의 then() 메소드를 통한 작업을 수행할 Consumer를 저장
- message: 구현체 출력 시 반환되는 문자열
</div>
</details>
</div>

<div align="center">
<details>
<summary><b>Constructor</b></summary>
<div align="left">

```java
public class Mocker<T> {
    // ...
    public Mocker() {}
    public Mocker(String message) {}
    // ...
}
```
</div>
</details>
</div>

<div align="center">
<details>
<summary><b>Method</b></summary>
<div align="left">

```java
public class Mocker<T> {
    // ...
    public <U> void mock(Function<T, U> mocked, Consumer<U> action) {}
    public <U, R> void mock(Function<T, U> mocked, Function<U, R> action, R result) {}
    public <U> void mockThrowable(Function<T, U> mocked, Consumer<U> action, Class<? extends Throwable> toBeThrown) {}

    public void given(T t) {}
    public void then(T t) {}
    public void add(Consumer<T> given, Consumer<T> then) {}
    public String toString() {}
}
```
- mock(), mockThrowable(): 모킹할 객체의 행동을 정의하는 메소드로, given 절과 then 절을 작성해주고 저장
- given(), then(): givens와 thens에 저장된 given 절과 then 절을 실행
- add(): 직접 작성한 given 절과 then 절을 각각 givens와 thens에 저장
</div>
</details>
</div>

### 모킹 상황
모킹을 수행하는 상황을 세 가지로 정리하면 다음과 같습니다.
- 모킹 대상 메소드의 반환 값이 존재하는 경우
```java
BDDMockito.given(articleRepository.findById(anyLong())).willReturn(response);
BDDMockito.then(articleRepository).should().findById(anyLong());
```

- 모킹 대상 메소드의 반환 값이 존재하지 않는 경우
```java
BDDMockito.willDoNothing().given(articleRepository).delete(any());
BDDMockito.then(articleRepository).should().delete(any());
```
- 모킹 대상 메소드가 예외를 던지는 경우
```java
BDDMockito.willThrow(RuntimeException.class).given(articleRepository).findById(anyLong());
BDDMockito.then(articleRepository).should().findById(anyLong());
```

### mock(), mockThrowable()
mock(), mockThrowable()을 사용하면, 모킹 객체에 접근하기 위한 Function, 모킹 대상 메소드, 대상 메소드가 반환할 값을 전달해 모킹 작업을 간결하게 정의할 수 있습니다.

```java
static Function<ArticleServiceTest, ArticleRepository> articleRepository = t -> t.mockArticleRepository;
// ...
Mocker<ArticleServiceTest> mocker = new Mocker<>("Example");
mocker.mock(articleRepository, a -> a.findById(anyLong()), Optional.of(article()));
mocker.mock(articleRepository, a -> a.delete(any()));
mocker.mockThrowable(articleRepository, a -> a.findById(anyLong()), RuntimeException.class);
```

### 적용
정적 팩토리 메소드에 적용한 예시는 다음과 같습니다.

#### Function 정의
정적(Static) 팩토리 메소드는 Mockito를 통해 만든 가짜 인스턴스에 접근할 수 없습니다. 
따라서 먼저, 테스트 클래스에 모킹 객체를 주입하면 동작을 수행할 수 있도록 모킹 객체에 접근하는 Function을 정의해야 합니다.

```java
static Function<ArticleServiceTest, ArticleRepository> articleRepository = t -> t.mockArticleRepository;  
static Function<ArticleServiceTest, ArticleCommentRepository> articleCommentRepository = t -> t.mockArticleCommentRepository;
static Function<ArticleServiceTest, AttachmentRepository> attachmentRepository = t -> t.mockAttachmentRepository;  
static Function<ArticleServiceTest, FileStore> fileStore = t -> t.mockFileStore;
```

#### Static Factory Method 작성
mock()을 사용해, mock 객체에 접근하기 위한 Function, stub 대상 메소드, 대상 메소드가 반환할 값을 Mocker 인스턴스에 전달합니다.
```java
static Stream<Arguments> getArticleTest() {
    Mocker<ArticleServiceTest> valid = new Mocker<>("id를 입력하면, 게시글과 관련 댓글 리스트를 반환");
    valid.mock(attachmentRepository, a -> a.findAllByArticle(any()), attachments());
    valid.mock(fileStore, f -> f.getUrls(any()), List.of(STORED_URL));
    valid.mock(articleRepository, a -> a.findById(anyLong()), Optional.of(article()));
    valid.mock(articleCommentRepository, a -> a.findAllByArticle(any(), any()), articleComments());

    Mocker<ArticleServiceTest> notFound = new Mocker<>("잘못된 id를 입력하면, 반환 없이 예외를 던짐");
    notFound.mock(articleRepository, a -> a.findById(any()), Optional.empty());

    return Stream.of(Arguments.of(valid), Arguments.of(notFound));
}
```

## Asserter
검증 대상 객체의 메소드를 실행한 이후에는 여러 값을 검증해야 합니다. Asserter는 정적 팩토리 메소드에서 검증 작업을 정의하고, 이를 테스트 메소드에서 실행하는 것을 돕습니다.

<div align="center">
<details>
<summary><b>Field</b></summary>
<div align="left">

```java
public class Asserter<T> {
    private final List<Runnable> runnables = new ArrayList<>();
    private final List<Consumer<T>> consumers = new ArrayList<>();
    private final List<Consumer<Throwable>> throwables = new ArrayList<>();
    private String message;
    // ...
}
```
- T: 타입  매개변수에는 검증 대상 메소드의 반환 타입을 전달(반환 값이 존재하는 경우)
- runnables: Runnable로 받을 수 있는 검증 코드를 저장
- consumers: Consumer로 받을 수 있는 검증 코드를 저장
- throwables: 예외 검증 코드를 저장
- message: 구현체 출력 시 반환되는 문자열

</div>
</details>
</div>
<div align="center">
<details>
<summary><b>Constructor</b></summary>
<div align="left">

```java
public class Asserter<T> {
    // ...
    public Asserter() {}
    public Asserter(String message) {}
    // ...
}
```
</div>
</details>
</div>
<div align="center">
<details>
<summary><b>Method</b></summary>
<div align="left">

```java
public class Asserter<T> {
    // ...
    public void add(Runnable Asserter) {}
    public void add(Consumer<T> Asserter) {}
    public void addThrowable(Consumer<Throwable> Asserter) {}

    public void execute() {}
    public void execute(T target) {}
    public void execute(Throwable t) {}

    public String toString() {}
}
```
- add(), addThrowable(): JUnit이나 AssertJ를 이용하는 검증 코드를 함수형 인터페이스로 받아 ArrayList에 저장하는 메소드
- execute(): 함수형 인터페이스로 저장한 검증 코드를 실행하는 메소드
</div>
</details>
</div>

### 검증 상황
검증을 수행하는 상황을 세 가지로 정리하면 다음과 같습니다.
- 대상 메소드를 실행하고 전달 받은 반환 값의 상태를 검증
```java
ArticleDto response = articleService.getArticle(request);
Assertions.assertThat(response.getArticleId()).isNotNull();
```
- 대상 메소드를 실행하고 특정 값(반환 값 X)의 상태를 검증
```java
ArticleDto response = articleService.getArticle(request);
Assertions.assertThat(differentTarget).hasFieldOrPropertyWithValue("target", "target");
```
- 대상 메소드를 실행하고 발생한 예외를 검증
```java
Throwable t = Assertions.catchThrowable(() -> articleService.getArticle(request));
assertThat(t).isInstanceOf(ArticleNotFoundException.class);
```

### add(), addThrowable()
add()와 addThrowable()로 검증 작업을 수행하는 람다식을 Asserter 인스턴스에 전달할 수 있습니다.
```java
    Asserter<ArticleDto> asserter = new Asserter<>("Example");
    asserter.add(dto -> assertThat(dto.getArticleId()).isNotNull());
    asserter.add(() -> assertThat(target).hasFieldOrPropertyWithValue("target", "target"));
    asserter.addThrowable(t -> assertThat(t).isInstanceOf(ArticleNotFoundException.class));
```

각 메소드의 파라미터는 다음과 같습니다.
- 반환 값 검증: Consumer<T>
- 특정 값 검증: Runnable
- 예외 검증: Consumer<Throwable>

### 적용
정적 팩토리 메소드에 적용한 예시는 다음과 같습니다.
```java
static Stream<Arguments> getArticleTest() {
    Mocker<ArticleServiceTest> validMocker= new Mocker<>("id를 입력하면, 게시글과 관련 댓글 리스트를 반환");
    validMocker.mock(attachmentRepository, a -> a.findAllByArticle(any()), attachments());
    validMocker.mock(fileStore, f -> f.getUrls(any()), List.of(STORED_URL));
    validMocker.mock(articleRepository, a -> a.findById(anyLong()), Optional.of(article()));
    validMocker.mock(articleCommentRepository, a -> a.findAllByArticle(any(), any()), articleComments());

    Asserter<ArticleDto> validAsserter = new Asserter<>("id를 입력하면, 게시글과 관련 댓글 리스트를 반환");
    validAsserter.add(dto -> assertThat(dto.getArticleId()).isNotNull());
    validAsserter.add(dto -> assertThat(dto.getArticleComments().getContent()).isNotEmpty());
    validAsserter.add(dto -> assertThat(dto.getAttachmentUrls()).first().isEqualTo(STORED_URL));

    Mocker<ArticleServiceTest> notFoundMocker= new Mocker<>("잘못된 id를 입력하면, 반환 없이 예외를 던짐");
    notFoundMocker.mock(articleRepository, a -> a.findById(any()), Optional.empty());

    Asserter<?> notFoundAsserter = new Asserter<>("잘못된 id를 입력하면, 반환 없이 예외를 던짐");
    notFoundAsserter.addThrowable(t -> assertThat(t).isInstanceOf(ArticleNotFoundException.class));

    return Stream.of(Arguments.of(validMocker, validAsserter),
                     Arguments.of(notFoundMocker, notFoundAsserter));
}
```
그러나 다음에 설명할 Context를 사용하면, Mocker와 Asserter을 직접 사용할 필요가 없습니다.


## Context
일반적으로 단위 테스트에서는 모킹과 검증 작업을 모두 수행합니다. 
퍼사드(Facade) 패턴을 구현하는 Context는 간편하게 모킹(Mocker)과 검증(Asserter) 작업을 정의하는 것을 돕습니다.

<div align="center">
<details>
<summary><b>Field</b></summary>
<div align="left">

```java
public class Context<M, A> {
    private final Mocker<M> mocker = new Mocker<>();
    private final Asserter<A> Asserter = new Asserter<>();
    private final String message;
    // ...
}
```
- Mocker, Asserter을 인스턴스로 가짐
</div>
</details>
</div>
<div align="center">
<details>
<summary><b>Constructor</b></summary>
<div align="left">

```java
public class Context<M, A> {
    // ...
    public Context(String message) {}
    // ...
}
```
</div>
</details>
</div>
<div align="center">
<details>
<summary><b>Method</b></summary>
<div align="left">

```java
public class Context<M, A> {
    // ...
    public <N> void willDo(Function<M, N> mocked, Consumer<N> action) {}
    public <N, O> void willDo(Function<M, N> mocked, Function<N, O> action, O result) {}
    public <N> void willThrow(Function<M, N> mocked, Consumer<N> action, Class<? extends Throwable> type) {}
    
    public void given(M mocked) {}
    public void then(M mocked) {}

    public void willBe(Runnable assertThat) {}
    public void willBe(Consumer<A> assertThat) {}
    public void willCatch(Consumer<Throwable> assertThat) {}

    public void doAssert() {}
    public void doAssert(A target) {}
    public void doAssert(Throwable throwable) {}

    @Override
    public String toString() {}
}
```
- willDo(), willThrow: Mocker의 mock(), mockThrowable()을 실행
- given(), then(): Mocker의 given(), then()를 실행
- willBe(), willCatch(): Asserter의 add(), addThrowable()를 실행
- doAssert(): Asserter의 execute()를 실행
</div>
</details>
</div>

### 적용
이전에 Mocker와 Asserter을 통해 수행하던 작업을 Context 인스턴스를 통해 수행할 수 있습니다.

```java
static Stream<Arguments> getArticleTest() {
    Context<ArticleServiceTest, ArticleDto> valid = new Context<>("id를 입력하면, 게시글과 관련 댓글 리스트를 반환");
    valid.willDo(articleRepository, a -> a.findById(anyLong()), Optional.of(article()));
    valid.willDo(attachmentRepository, a -> a.findAllByArticle(any()), attachments());
    valid.willDo(fileStore, f -> f.getUrls(any()), List.of(STORED_URL));
    valid.willDo(articleCommentRepository, a -> a.findAllByArticle(any(), any()), articleComments());
    
    valid.willBe(dto -> assertThat(dto.getArticleId()).isNotNull());
    valid.willBe(dto -> assertThat(dto.getArticleComments().getContent()).isNotEmpty());
    valid.willBe(dto -> assertThat(dto.getAttachmentUrls()).first().isEqualTo(STORED_URL));

    Context<ArticleServiceTest, ?> notFound = new Context<>("잘못된 id를 입력하면, 반환 없이 예외를 던짐");
    notFound.willDo(articleRepository, a -> a.findById(any()), Optional.empty());
    notFound.willCatch(t -> assertThat(t).isInstanceOf(ArticleNotFoundException.class));

    return Stream.of(Arguments.of(valid), Arguments.of(notFound));
}
```

## TestTemplate
TestTemplate는 Context를 전달 받아 모킹 객체를 주입해 실행하고, 테스트 대상 메소드를 실행한 후 여러 값을 검증하는 작업을 돕습니다.
일반적인 테스트 메소드에서 수행하는 작업을 순서대로 처리합니다.

<div align="center">
<details>
<summary><b>Method</b></summary>
<div align="left">

```java
public class TestTemplate {
    public static <T> void doTest(Runnable toBeTested, Context<T, ?> context, T mocked) {}
    public static <T, R> void doTest(Supplier<R> toBeTested, Context<T, R> context, T mocked) {}
    private static <T, R> Runnable perform(Context<T, R> context, T mocked, Runnable runnable) {}
    }
```
- doTest(): 검증 대상 메소드, Context 인스턴스, 모킹 객체를 전달 받아, 테스트 작업을 수행

</div>
</details>
</div>

### doTest()
단위 테스트 메소드에서는 **'의존 객체 모킹 - 검증 대상 메소드 실행 - 값 검증'** 작업을 수행해야 합니다.
이 작업은 일반적인 단위 테스트 메소드에서 수행하는 공통적인 작업이므로, 제네릭을 사용해 추상화할 수 있습니다.  

테스트 상황에 구애받지 않고 검증 대상 메소드, Context 인스턴스, 모킹 객체에 접근할 수 있는 인스턴스를 전달하면, doTest()는 순서대로 테스트 작업을 수행합니다.

### 적용
테스트 메소드를 작성해, 정적 팩토리 메소드로부터 전달 받은 Context 인스턴스, 검증 대상 메소드를 실행할 람다식, 모킹 인스턴스를 가지고 있는 인스턴스를 TestTemplate의 doTest()로 전달합니다.

```java
@DisplayName("게시글 단건 조회")  
@ParameterizedTest(name = "{index}: {0}")  
@MethodSource  
void getArticleTest(Context<ArticleServiceTest, ArticleDto> context) {  
    TestTemplate.doTest(() -> articleService.getArticle(articleDto()), context, this);  
}
static Stream<Arguments> getArticleTest() {}
```

## 적용 결과
적용 전 getArticle()의 정상 호출과 비정상 호출(잘못된 게시글 id)을 검증하는 메소드는 다음과 같습니다.

### 적용 전
```java
@DisplayName("[조회] id를 입력하면, 게시글과 관련 댓글 리스트를 반환")
@Test
void givenArticleId_whenSearchingArticle_thenReturnsArticle() {
    //Given
    given(mockAttachmentRepository.findAllByArticle(any())).willReturn(attachments());
    given(mockFileStore.getUrls(any())).willReturn(List.of(STORED_URL));
    given(mockArticleRepository.findById(anyLong())).willReturn(Optional.of(article()));
    given(mockArticleCommentRepository.findAllByArticle(any(), any())).willReturn(articleComments());

    // When
    ArticleDto dto = articleService.getArticle(articleDto());

    // Then
    assertThat(dto.getArticleId()).isNotNull();
    assertThat(dto.getArticleComments().getContent()).isNotEmpty();
    assertThat(dto.getAttachmentUrls()).first().isEqualTo(STORED_URL);

    then(mockAttachmentRepository).should().findAllByArticle(any());
    then(mockFileStore).should().getUrls(any());
    then(mockArticleRepository).should().findById(anyLong());
    then(mockArticleCommentRepository).should().findAllByArticle(any(), any());
}

@DisplayName("[조회] 잘못된 id를 입력하면, 반환 없이 예외를 던짐")
@Test
void givenWrongArticleId_whenSearchingArticle_thenThrowsException() {
    //Given
    given(mockArticleRepository.findById(anyLong())).willReturn(Optional.empty());

    // When
    Throwable t = catchThrowable(() -> articleService.getArticle(articleDto()));

    // Then
    assertThat(t).isInstanceOf(ArticleNotFoundException.class);
    then(mockArticleRepository).should().findById(anyLong());
}
```

### 적용 후
적용 후 getArticle()의 정상 호출과 비정상 호출(잘못된 게시글 id)을 검증하는 메소드는 다음과 같습니다.

```java
@DisplayName("게시글 단건 조회")  
@ParameterizedTest(name = "{index}: {0}")  
@MethodSource  
void getArticleTest(Context<ArticleServiceTest, ArticleDto> context) {  
    TestTemplate.doTest(() -> articleService.getArticle(articleDto()), context, this);  
}
static Stream<Arguments> getArticleTest() {
    Context<ArticleServiceTest, ArticleDto> valid = new Context<>("id를 입력하면, 게시글과 관련 댓글 리스트를 반환");
    valid.willDo(articleRepository, a -> a.findById(anyLong()), Optional.of(article()));
    valid.willDo(attachmentRepository, a -> a.findAllByArticle(any()), attachments());
    valid.willDo(fileStore, f -> f.getUrls(any()), List.of(STORED_URL));
    valid.willDo(articleCommentRepository, a -> a.findAllByArticle(any(), any()), articleComments());

    valid.willBe(dto -> assertThat(dto.getArticleId()).isNotNull());
    valid.willBe(dto -> assertThat(dto.getArticleComments().getContent()).isNotEmpty());
    valid.willBe(dto -> assertThat(dto.getAttachmentUrls()).first().isEqualTo(STORED_URL));

    Context<ArticleServiceTest, ?> notFound = new Context<>("잘못된 id를 입력하면, 반환 없이 예외를 던짐");
    notFound.willDo(articleRepository, a -> a.findById(any()), Optional.empty());
    notFound.willCatch(t -> assertThat(t).isInstanceOf(ArticleNotFoundException.class));

    return Stream.of(Arguments.of(valid), Arguments.of(notFound));
}
```

# Implementation Details
## Mocker
Mocker에는 두 가지 주요 구현 사항이 있습니다.
- 모킹 대상 객체, 메소드 대한 정보를 받아 given(), then()을 사용하는 람다식('given 절', 'then 절')을 작성
- given 절과 then 절에 모킹 인스턴스를 주입해 실행

### given, then 절 만들기
Mocker가 만들어야 하는 given/then 절을 람다식으로 표현하면 다음과 같습니다.

```java
Runnable willReturn    = () -> BDDMockito.given(articleRepository.findById(anyLong())).willReturn(response);
Runnable willDoNothing = () -> BDDMockito.willDoNothing().given(articleRepository).delete(any());
Runnable willThrow     = () -> DDMockito.willThrow(Exception.class).given(articleRepository).findById(anyLong());

Runnable thenReturn    = () -> BDDMockito.then(articleRepository).should().findById(anyLong());
Runnable thenDoNothing = () -> BDDMockito.then(articleRepository).should().delete(any());
Runnable thenThrow     = () -> BDDMockito.then(articleRepository).should().findById(anyLong());
```

ArticleRepository를 외부에서 주입 받는 형태로 위 코드를 변경하면 다음과 같습니다.
```java
Consumer<ArticleRepository> willReturn    = a -> BDDMockito.given(a.findById(anyLong())).willReturn(response);
Consumer<ArticleRepository> willDoNothing = a -> BDDMockito.willDoNothing().given(a).delete(any());
Consumer<ArticleRepository> willThrow     = a -> DDMockito.willThrow(Exception.class).given(a).findById(anyLong());

Consumer<ArticleRepository> thenReturn    = a -> BDDMockito.then(a).should().findById(anyLong());
Consumer<ArticleRepository> thenDoNothing = a -> BDDMockito.then(a).should().delete(any());
Consumer<ArticleRepository> thenThrow     = a -> BDDMockito.then(a).should().findById(anyLong());

```

위 코드에서 검증 대상 메소드를 따로 추출하면 다음과 같습니다.
```java
Function<ArticleRepository, Optional<Article>> doReturn = a -> a.findById(anyLong());
Consumer<ArticleRepository> doNothing = a -> a.delete(any());
Consumer<ArticleRepository> doThrow = a -> a.findById(anyLong());

Consumer<ArticleRepository> willReturn    = a -> BDDMockito.given(doReturn.apply(a)).willReturn(response);
Consumer<ArticleRepository> willDoNothing = a -> doNothing.accept(BDDMockito.willDoNothing().given(a));
Consumer<ArticleRepository> willThrow     = a -> doThrow.accept(BDDMockito.willThrow(Exception.class).given(a));

Consumer<ArticleRepository> thenReturn    = a -> doReturn.apply(BDDMockito.then(a).should());
Consumer<ArticleRepository> thenDoNothing = a -> doNothing.accept(BDDMockito.then(a).should());
Consumer<ArticleRepository> thenThrow     = a -> doThrow.accept(BDDMockito.then(a).should());
```

위 코드에 제네릭을 사용해 추상화를 수행하고, 이를 바탕으로 작성한 메소드가 바로 mock()와 mockThrowable()입니다.
```java
 public <U> void mock(Function<T, U> mocked, Consumer<U> action) {
     Consumer<T> given = t -> action.accept(BDDMockito.willDoNothing().given(mocked.apply(t)));
     Consumer<T> then  = t -> action.accept(BDDMockito.then(mocked.apply(t)).should());
     add(given, then);
 }
 public <U, R> void mock(Function<T, U> mocked, Function<U, R> action, R result) {
     Consumer<T> given = t -> BDDMockito.given(action.apply(mocked.apply(t))).willReturn(result);
     Consumer<T> then  = t -> action.apply(BDDMockito.then(mocked.apply(t)).should());
     add(given, then);
 }
 public <U> void mockThrowable(Function<T, U> mocked, Consumer<U> action, Class<? extends Throwable> toBeThrown) {
     Consumer<T> given = t -> action.accept(BDDMockito.willThrow(toBeThrown).given(mocked.apply(t)));
     Consumer<T> then  = t -> action.accept(BDDMockito.then(mocked.apply(t)).should());
     add(given, then);
 }
```

### 모킹 인스턴스 주입 및 실행
given()과 then()은 givens와 thens에 저장된 given, then 절에 전달 받은 객체를 주입하며 실행시킵니다.
```java
private final List<Consumer<T>> givens = new ArrayList<>();
private final List<Consumer<T>> thens = new ArrayList<>();

 public void given(T t) {
     givens.forEach(given -> given.accept(t));
 }
 public void then(T t) {
     thens.forEach(then -> then.accept(t));
 }
```
## Asserter
Asserter에는 두 가지 주요 구현 사항이 있습니다.
- 전달 받은 검증 람다식을 저장
- 검증 람다식을 실행

### 전달 받은 검증 람다식 저장
검증을 수행하는 세 가지 상황을 구분하기 위해, Asserter는 Runnable, Consumer<T>, Consumer<Throwable>를 각각 List에 저장합니다.
```java
private final List<Runnable> runnables = new ArrayList<>();             // 특정 값의 상태를 검증
private final List<Consumer<T>> consumers = new ArrayList<>();          // 반환 값의 상태를 검증
private final List<Consumer<Throwable>> throwables = new ArrayList<>(); // 예외 검증

public void add(Runnable assertion) {
    runnables.add(assertion);
}
// ... 생략
```

### 검증 람다식 실행
검증 람다식을 실행할 때는 반환 값이 아닌 값을 검증하는 경우를 제외하고 모두 검증에 필요한 값을 전달 받습니다.  
반환 값이 아닌 값은 이미 람다식에 들어가 있어, 파라미터로 전달할 필요가 없습니다.
```java
public void execute() {
    runnables.forEach(Runnable::run);
}
public void execute(T target) {
    consumers.forEach(a -> a.accept(target));
}
public void execute(Throwable t) {
    throwables.forEach(a -> a.accept(t));
}
```


## Context
Context는 내부의 Mocker, Asserter를 은닉하기 위해 각 인스턴스의 메소드를 호출하는 메소드를 가지고 있습니다.
- Mocker 인스턴스 메소드 호출
```java
public class Context<M, A> {
    private final Mocker<M> mocker = new Mocker<>();
    // ...
    
    public <N> void willDo(Function<M, N> mocked, Consumer<N> action) {
        mocker.mock(mocked, action);
    }
    public <N, O> void willDo(Function<M, N> mocked, Function<N, O> action, O result) {
        mocker.mock(mocked, action, result);
    }
    public <N> void willThrow(Function<M, N> mocked, Consumer<N> action, Class<? extends Throwable> toBeThrown) {
        mocker.mockThrowable(mocked, action, toBeThrown);
    }

    public void given(M mocked) {
        mocker.given(mocked);
    }
    public void then(M mocked) {
        mocker.then(mocked);
    }
}
```
- Asserter 인스턴스 메소드 호출
```java
public class Context<M, A> {
    private final Asserter<A> asserter = new Asserter<>();
    // ...

    public void willBe(Runnable assertion) {
        asserter.add(assertion);
    }
    public void willBe(Consumer<A> assertion) {
        asserter.add(assertion);
    }
    public void willCatch(Consumer<Throwable> assertion) {
        asserter.addThrowable(assertion);
    }

    public void doAssert() {
        asserter.execute();
    }
    public void doAssert(A target) {
        asserter.execute(target);
    }
    public void doAssert(Throwable throwable) {
        asserter.execute(throwable);
    }
}
```
    
## TestTemplate
### perform()
테스트 대상 메소드가 반환 값이 있는 경우와 없는 경우 모두 대비하기 위해 두 개의 doTest() 메소드를 작성했지만, 두 메소드가 실행할 전체적인 작업의 순서는 동일합니다(의존 객체 모킹 - 검증 대상 메소드 실행 - 값 검증).

테스트 작업 순서의 동일성을 보장하기 위해, doTest()는 특정 작업을 작성된 순서 내에서 수행하는 perform() 메소드를 사용합니다.
```java
private static <T, R> Runnable perform(Context<T, R> context, T mocked, Runnable runnable) {
    return () -> {
        context.given(mocked);
        try {
            runnable.run();
            context.doAssert();
        } catch (Throwable t) {
            context.doAssert(t);
        }
        context.then(mocked);
    };
}
```

perform()이 수행하는 작업은 다음과 같습니다.
- given 절 실행(의존 객체 모킹)
- 전달 받은 람다식 실행(검증 대상 메소드 실행)
- 값/예외 검증
- then 절 실행(모킹 객체 실행 여부 검증)


### doTest()
perform() 메소드에서 테스트에 필요한 대부분의 작업을 수행해주므로, doTest()에서는 최소한의 작업만 전달하면 됩니다. 

다만 반환 값이 있는 메소드를 검증하는 경우에는 반환 값에 대한 검증도 필요하므로, 실행 결과를 context의 doAssert(A target)로 감싸서 전달합니다.
```java
public static <T> void doTest(Runnable toBeTested, Context<T, ?> context, T mocked) {
    perform(context, mocked, toBeTested).run();
}

public static <T, R> void doTest(Supplier<R> toBeTested, Context<T, R> context, T mocked) {
    perform(context, mocked, () -> context.doAssert(toBeTested.get())).run();
}
```

# Related Posts
[🧰[No Boilerplate] 테스트의 코드 중복을 없애보자](https://blog.naver.com/boldfaced7/223409525705)

0. 개요: 왜 이런 짓을 하게 되었나  
   1. [TDD 도입과 귀찮은 짓](https://blog.naver.com/boldfaced7/223409575416)  
   2. [@ParameterizedTest와 제네릭 메소드](https://blog.naver.com/boldfaced7/223409667290)  
   3. [인간의 욕심은 끝이 없고](https://blog.naver.com/boldfaced7/223409738286)  


1. Mocker 작성: 모킹 객체 주입하기  
   1. [모킹이 왜 안 돼?](https://blog.naver.com/boldfaced7/223409904428)  
   2. [given()/then() 코드 분석](https://blog.naver.com/boldfaced7/223413569741)  
   3. [만들 수 있어 보이는데 말이야](https://blog.naver.com/boldfaced7/223413679854)  


2. Mocker 리팩토링: 모킹 객체'들' 주입하기  
   1. [Service가 Facade라니까요?](https://blog.naver.com/boldfaced7/223413842266)  
   2. [뭘 주입할지 알려줘](https://blog.naver.com/boldfaced7/223414396758)  
   3. [그건 제 잔상입니다만](https://blog.naver.com/boldfaced7/223414722678)  
   4. [되긴 되는데요](https://blog.naver.com/boldfaced7/223415344168)  


3. Asserter 작성: 결과 검증하기  
   1. [뭘 할지는 알아야지](https://blog.naver.com/boldfaced7/223416265237)  
   2. [만들어서 바로 적용](https://blog.naver.com/boldfaced7/223416490721)  


4. Context 작성: 리팩토링  
   1. [마른 오징어도 짜면 물이 나와요](https://blog.naver.com/boldfaced7/223416573186)  
   2. [코드로 보여주겠다](https://blog.naver.com/boldfaced7/223416623426)  
   3. [진짜로 보여주겠다](https://blog.naver.com/boldfaced7/223417352537)  
