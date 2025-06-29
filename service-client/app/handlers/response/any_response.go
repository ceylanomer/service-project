package response

type AnyResponse struct {
	Data    interface{}  `json:"data,omitempty"`
	Summary *BulkSummary `json:"summary,omitempty"`
}

type BulkResult struct {
	ServiceId string           `json:"serviceId"`
	Success   bool             `json:"success"`
	Data      *ServiceResponse `json:"data,omitempty"`
	Error     string           `json:"error,omitempty"`
}

type BulkSummary struct {
	TotalRequests    int   `json:"totalRequests"`
	SuccessfulCount  int   `json:"successfulCount"`
	FailedCount      int   `json:"failedCount"`
	ProcessingTimeMs int64 `json:"processingTimeMs"`
}
