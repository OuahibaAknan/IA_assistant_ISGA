import { render, screen } from "@testing-library/react";
import App from "./App";

test("renders chatbot heading", () => {
  render(<App />);
  const headingElement = screen.getByText(/chatbot isga/i);
  expect(headingElement).toBeInTheDocument();
});
