package requests

type RetrieveServiceRequest struct {
	ServiceId  string   `json:"serviceId,omitempty"`  // Single service ID
	ServiceIds []string `json:"serviceIds,omitempty"` // Bulk service IDs
}
