# Growing Object-Oriented Software, Guided by Tests — Java Practice

這個專案用來逐步重現《Growing Object-Oriented Software, Guided by Tests》（GOOS）的測試驅動開發過程。

本專案從真正的空白 Git 起點開始，不直接複製既有完成版，而是依照需求、測試與實際開發過程逐步演進。

## Project Principles

### 1. Baby Step

Baby Step 是一個**可觀察的行為增量**，而不是固定的一個 method、class 或 file。

一個 Baby Step 可以跨越：

```text
E2E → Application → Domain / Collaborator → Infrastructure
```

我們只加入目前需求所需要的最小變化。

### 2. Stair-step Test

在開始真正的 Auction Sniper 行為之前，先建立最小的 Java 測試，確認測試工具鏈本身可以正常執行。

第一個測試的目的不是驗證產品功能，而是回答：

> Java 測試環境是否真的能正常執行 assertion？

這可以避免後續 E2E / Unit Test 失敗時，把「測試環境問題」誤認為「production code 問題」。

### 3. RED → GREEN → REFACTOR

優先保留清楚的 TDD 演進：

1. RED — 新增一個會失敗的測試。
2. GREEN — 用最小實作讓測試通過。
3. REFACTOR — 當目前設計已出現明確問題時改善設計。

不為形式製造沒有意義的 commit。

### 4. Green 後的設計判斷

Green 之後檢查目前設計：

- 如果下一個行為可以自然加入 → 繼續下一個 RED。
- 如果下一個增量暴露出特殊、複雜、重複或難以擴充的設計 → 先 REFACTOR。
- 不為尚未出現的需求預先建立架構。

### 5. Outside-In

從使用者可觀察的行為開始：

```text
E2E
 ↓
Application
 ↓
Domain / Collaborators
 ↓
Infrastructure
```

第一個 E2E 測試只建立最小 Walking Skeleton，不預先猜完整架構。

### 6. E2E → Unit Test

Unit Test 不是為了 method coverage 而建立。

當 E2E 開始暴露：

- 複雜邏輯
- 複雜物件協作
- 測試成本過高
- 暴力 Green 已經不容易維持

才進一步萃取 Unit Test。

### 7. Test Double / London School

當需要隔離 collaborator 時使用 Test Double。

如果測試的重點是物件之間的協作，優先使用 interaction-based test。

但不因為採用 London School 就 mock 所有物件。

### 8. 保留 GOOS 原書測試意圖

GOOS 原書中的測試表達方式具有重要的設計意圖，因此優先保留。

例如 JMock：

```java
context.checking(new Expectations() {{
    oneOf(listener).auctionClosed();
}});
```

這種寫法直接表達：

> 物件應該如何與 collaborator 協作。

除非有明確理由，否則不為了現代化而改變測試的意圖表達方式。

### 9. 測試必須驗證實際行為

可以在 production code 中採取最直接、甚至暫時粗糙的實作來取得 Green。

但不能修改 production code 到只是讓 compiler 或 test framework 通過，而沒有驗證真正的行為。

### 10. Final Implementation Reference

本專案的最終套件版本、工具選擇、環境設定與實作方式，以既有完成版：

`letterliu/goos-java-practice`

的 `main` branch 最終實作為準。

因此：

- 不自行引入與 `main` 不同的套件版本。
- 不自行替換 `main` 已採用的工具或實作方式。
- 不自行設計另一套最終環境。
- 可以依照 Baby Step 的需求分階段引入環境與依賴，但最終結果應與 `main` 的實作一致。

如果目前的 Baby Step 尚未需要某項最終環境設定，則不需要提前引入。

在尚未實際查證 `main` 的具體實作之前，不自行猜測版本、工具或設定。

## Reference Sources

### Primary Reference

主要思想與測試驅動開發方式來自：

Growing Object-Oriented Software, Guided by Tests

Authors: Steve Freeman and Nat Pryce

## Reference Repository

既有 GOOS Java 實作可用來觀察測試與程式碼的演進方式。

Reference repository：

`https://github.com/titangene/goos-java`

Reference branch：

```text
baby-step-v1
```

Reference repository 主要用來觀察 GOOS 的 TDD 演進與實作思路，不是本專案的規格，也不會盲目複製。

當 reference 的實作方式與本專案共識、letterliu/goos-java-practice 的 main 最終實作，或目前實際需求不同時，以本專案的決策為準。

如果採用 reference 中的某種實作方式，應先確認它是否符合目前的需求與共識。

## Completed Project Reference

既有完成版 repository 是本專案最終實作的參考基準。

Repository：

`https://github.com/letterliu/goos-java-practice`

Main branch：

`main`

Main branch commit history：

`https://github.com/letterliu/goos-java-practice/commits/main/`

`main` 主要用來確認：

- 套件與依賴版本
- Java / JDK 環境
- Build 與測試工具
- Docker 設定
- 資料夾結構
- 測試工具設定
- Infrastructure 實作
- 其他最終環境設定

以上內容皆以 `main` 的實際實作為準，不自行設計不同的最終方案。

這些內容不會預先搬入空白起點。

只有當目前的 Baby Step 需要相關設定時，才從 `main` 確認並引入目前真正需要的部分。

如果 `main` 的具體版本或實作尚未查證，不能自行猜測或替換成其他版本與工具。